package husart.solr.openinghours;

import java.io.IOException;
import java.util.Calendar;
import java.util.Map;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.BoolDocValues;

public class OpeningHoursFunction extends ValueSource {
	protected final ValueSource field;
	
	final public int date;
	final public int hour;
	public int year;
	
	public static final int ZHH = 9;
	public static final int DHH = 12;
	public static final int ZDDHH = 17;
	
	public OpeningHoursFunction(ValueSource field, int date, int hour) {
		
		Calendar calendar = Calendar.getInstance();
		this.field = field;
		this.date = date;
		this.hour = hour;
		this.year = calendar.get(Calendar.YEAR);  // current year
	}
	public OpeningHoursFunction(ValueSource field, int date, int hour, int year) {
		this(field, date, hour);
		this.year = year;
	}

	@Override
	public String description() {
		return "";
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof OpeningHoursFunction) {
			final OpeningHoursFunction obj = (OpeningHoursFunction) o;
			return this.field.equals(obj.field) && this.date == obj.date  && this.hour == obj.hour  && this.year == obj.year;
		}
		return false;
	}
	
	public static boolean isOpen(byte[] bytes, int date, int hour, int year) {
		int max = bytes.length;
		if (max == 0) {
			return false;
		}
		long nr = 0; //interval
		int length = 0; // type
		boolean close = false;		
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, date / 100 - 1, date % 100);
		int currentD = calendar.get(Calendar.DAY_OF_WEEK);
		calendar = null;
		
		// Because Sunday is 1, we must decrement and set Sunday to 7, so that Monday is 1
		if (--currentD == 0) {
			currentD = 7;
		}
		int cType = 0; // will be use for checking if the next interval has the same type
		int firstDigit = 0;	// will be use for day of week
		
		for (int i = 0; i < max; i++) {
			if (bytes[i] == '-') { // check if is closed
				close = true;
				continue;
			}
			// Check if is a number
			if (bytes[i] >= '0' && bytes[i] <= '9') {
				if (firstDigit == 0) { // set firstDigit with the day of week
					firstDigit = bytes[i] - '0';
				}
				nr = nr * 10 + bytes[i] - '0';  
				length++;
			} else if (bytes[i] == ';') { // finished format interval
				if (close) {
					if (date == nr || (length > 4 &&  date >= nr / 10000 && date <= nr %10000) ) {
						return false;
					}
					close = false;
					continue;
				}
				
				if (cType > 0 && length != cType) { // not the same type as previous
					return false;
				}
				
				// Check if is open
				switch (length) {
				case DHH:  
					if (date == nr / 100000000L) {
						if (hour <= (nr % 10000L) && hour >= (nr / 10000L) % 10000L) {
							return true;
						}
						cType = DHH;
					}
					break;
				case ZDDHH:
					if (firstDigit == currentD
							&& date >= (nr / 1000000000000L) % 10000
							&& date <= (nr / 100000000L) % 10000) {

						if (hour >= (nr % 100000000L) / 10000 && hour <= (nr % 10000)) {
							return true;
						}
						
						cType = ZDDHH;
					}

					break;
				case ZHH:
					if (currentD == firstDigit) {
						if (hour >= (nr % 100000000L) / 10000L && hour <= (nr % 10000L)) {
							return true;
						}
					}

					break;
				}
				// Reset
				nr = firstDigit = length = 0;
				
			}
		}

		return false;

	}

	@Override
	public BoolDocValues getValues(@SuppressWarnings("rawtypes") Map context,
			AtomicReaderContext readerContext) throws IOException {

		final FunctionValues fieldValue = field.getValues(context, readerContext);
		
		return new BoolDocValues(field) {
			
			@Override
			public boolean boolVal(int doc) {
				String code = fieldValue.strVal(doc); // get field value
				if (code == null) {
					return false;
				}
			
				/*
				char[] check = code.toCharArray();
				if(check[check.length - 1] != ';') {
					code += ";";					
				}
				*/
				return isOpen(code.getBytes(), date, hour, year);
			}
			
		};
		
	}

	@Override
	public int hashCode() {
		return field.hashCode() + date * 10000 + hour + year;
	}

}
