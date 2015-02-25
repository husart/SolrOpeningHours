package husart.solr.openinghours;

import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.ValueSourceParser;

public class OpeningHoursParser  extends ValueSourceParser {

	@Override
	public ValueSource parse(FunctionQParser fqp) throws SyntaxError {
	
		ValueSource fieldName = fqp.parseValueSource();
		int date = fqp.parseInt();
		int hour = fqp.parseInt();
		
		if(fqp.hasMoreArguments()){
			return new OpeningHoursFunction(fieldName, date, hour,fqp.parseInt());
		}
		return new OpeningHoursFunction(fieldName, date, hour);
	}
}