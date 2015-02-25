# Solr Opening Hours
===============

This plugin provides a Solr FunctionQueries that parses opening hours and checks if is open at a date and a specified hour. It returns a boolean.

Currently this repo contains an eclipse project but will be changed soon. You can download the project and import it in your eclipse workspace.

## How to build

Export the project as a jar (let's say openinghoursfilter.jar).

Navigate to Solr home directory (or if you are using multicores navigate to desired core folder).

Create a folder named `lib` (in the same folder which contains `conf` and `data` folders).

Copy `openinghoursfilter.jar` (your exported jar) and  the files from `lib` project folder into the previously created `lib` folder.

Edit `solrconfig.xml` and add the following lines directly under the `<config>` tag.

```xml
<lib dir="../lib/" regex=".*\.jar" />

<valueSourceParser name="openingHoursFCT"
      class="husart.solr.openinghours.OpeningHoursParser" />  
```
  
`openingHoursFCT` is the function that will be use in your query, you can change the name.

You can now restart solr.

## How to index

`Z` - day of the week: numeric representation 1 (for Monday) through 7 (for Sunday)

`H` - hour: for exemple 1200 means 12:00

`D` - day: it contains month and day for exemple 0212 means 12 February

There are 4 types:
  1. Default - `ZHH`  representing the default hour composed from the day of the week and  opening hours interval, ex 108001800 means Monday from 8:00 to 18:00 is open.
  2. Special days - `ZDDHH` representing the opening hours for a day from an interval > 7 days, ex 10212031208001800 means between 12 February and 12 March every Monday from 8:00 to 18:00 is open.
  3. Special day - `DHH` representing the interval for a day, ex 0212081800 means 12 February from 8:00 to 18:00 is open.
  4. Close day/Close interval - `-D`, `-DD`  must be a negative number representing the day or interval, ex : -0212 means 2 February is closed, or 02120312 means from 12 February to 12 March is closed.

Priority order: -D, -DD, DHH, ZDDHH, ZHH.

Your field must be the type string and not multivalue.

The content must be sorted by priority and intervals delimited by `;`. In `php` folder you can find the function `opening_hours_normalize()` wich receives an array of intervals and return a string sorted by priority.

For ex:
 if you have the next intervals:
 
 ```php
[
  '10212031208001800',
  '-0213',
  '102120312019002000',
  '20212031208001800',
  '209001200',
  '109001200',
  '409001200',
  '031312001400',
  '309001200',
  '509001200',
  '30212031208001800',
  '40212031208001800',
  '50212031208001800'
]
```
 the function will return `'-0213;031312001400;50212031208001800;40212031208001800;30212031208001800;20212031208001800;10212031208001800;509001200;409001200;309001200;209001200;109001200;'`.

After indexing data you can test in your query:
`{!frange l=1}YOUR_FUNC_NAME(YOUR_FIELD, DATE, HOUR, YEAR)`, where year is optional, default is current year - ex `{!frange l=1}openingHoursFCT(hours_field, 212,1300)` means 12 February at 13:00.

The function will return only the open ones. If you want to get the closed ones, just replace l=1 with u=0 inside the frange. Ex: `{!frange u=0}YOUR_FUNC_NAME(YOUR_FIELD, DATE, HOUR, YEAR)`
 
## TODO

 - support for multivalued fields
 - json parser
 - more examples

 