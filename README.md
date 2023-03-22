## Project Details
- ### Project name: Server
- ### Team members
	- James Li (sli64, 28 hrs)
	- Angie Wang (lwang58, 24 hrs)
- ### Contributions
	- Functionalities
		- loadcsv (Angie+James)
		- viewcsv (Angie)
		- searchcsv (Angie+James)
		- weather (James+Angie)
	- Debugging (James+Angie)
	- Refactoring and Optimization (James)
	- Unit testing (Angie+James)
	- Integration testing (Angie+James)
	- Documentation (James)
- ### [Link to repo](https://github.com/cs0320-s2023/sprint-3-lwang58-sli64.git)

## Design choices
- Patterns, Organization, Dependency Injection
	- modularized objects and functions into packages, interfaces, mutable classes and immutable records in a way that implemented inheritance
- Defensive Programming and Error Handling
	- made all responses to immutable record types
	- used APIFailureException to pass on error message back to routers and generate error response
	- prevented malicious attacks to file system by preventing access to upward directory
	- restricted access control in server
	- caught error_bad_json if requested by unexpected endpoints
	- used iterator to pass csvParser to different CsvRouter
- Web APIs
	- followed NWS's 3 step process to fetch metadata and forecast
	- fetched JSON object NWS API
	- parsed objects to acquire fields of interests

## Errors/Bugs
- none remaining bugs

## Instructions
- run `server/Server` in Intellij, and uses the following basepoint to access `http://127.0.0.1:3232/`+`endpoint`+`?`+`param1=value1`+`&`+`param2=value2`, etc.
- available endpoints and parameters
	- `loadcsv`:
		- parameters
			- filepath, no `..` allowed
			- header, `true` or `false`
		- example: http://127.0.0.1:3232/loadcsv?filepath=stars/stardata.csv&header=false
	- `viewcsv`
		- examples: http://127.0.0.1:3232/viewcsv
	- `searchcsv`: query, mode (fuzzy or exact)
		- parameters
			- query, basic ones should be`value@columnName` or `value@columnIndex`, composite ones should be `AND(q1,q2)`, `OR(q1,q2)`, `NOT(q1,q2)` with each composite query nested in one of the `q` if desired
		- examples:
			- http://127.0.0.1:3232/searchcsv?query=Sol@ProperName&mode=fuzzy
			- http://127.0.0.1:3232/searchcsv?query=Rigel%20Kentaurus@ProperName&mode=fuzzy
			- http://127.0.0.1:3232/searchcsv?query=AND(Rigel%20Kentaurus@1,71454@0)&mode=fuzzy
	- `weather`:
		- parameters:
			- lat, -90 to +90
			- lon, -180 to +180
			- dt, (datetime, optional)
		- example: http://127.0.0.1:3232/weather?lat=35&lon=-78&dt=2023-03-05T06:00:00-06:00
- to understand how to incorporate this project into your own code or work on top of it
	- carefully read this readme, along with the documentation in
		- `server/Server.java`
		- `weather/WeatherRouter.java`
		- all the csvRouters in `csv/routers/`
	- after that, read everything in the `shared` package as they detailed how responses are generated
		- `APIFailureException` is to pass on error message from children classes all the way back to routers which catch the exceptions and generate failure response
		- `APIResponse` is implemented by SuccessResponse and FailureResponse in both csv and weather, i.e. a total of 4 concrete immutable record type Responses
	- then consider reading the `handler` packages inside `csv` and `weather` which details how weather/csv data are fetched

## User Stories
- ### story1
	- `viewcsv` and `searchcsv` will return error response if no csv is selected
- ### story2
	- `weather` finds temperatures based on given latitude, longitude and optional dateTime
- ### story3
	- failed weatherData are also stored in the cache to prevent unnecessary calling
	- evicts cached weatherData after 60 minutes without accessing
- ### story4
	- carefully read the **Instructions** above for better grasp of this project
	

## Tests
- integration test API server's behaviors, responding to
	- correct requests
	- ill-formed requests
	- missing/ill-formed field requests
	- inexistent datasource requests
- unit test new code
	- sending/processing API messages from NWS API
	- managing caching
	- use mocking to avoid repeated calls to NWS API
- Unit testing
- Integration testing
  Unit testing examples below, more in tests (created mocking nws api for unit tests)
  anded on top of csv tests in sprint 1
  t if returns jsonobject successfully
  tWithMockData for weather
  testWithOutMockData for weather
  testWithOutMockDataThrowException which is when latitude, longitude has wrong input
  testFindWeatherWithMockNwsApi
  testCache
  testCacheWithException


- Integration testing  examples below: more in tests
  testLoadcsvSuccess when csv is loadded successfully
  testLoadcsvTwice when I load 2 different csvs
  testLoadcsvFail when loading csv fails
  testLoadcsvFailWithOutFilepath when filepath does not exists
  testWebRequestSearchCsvFileNotLoaded
  loadCsv tests to check if it loads csv successfully
  testSearchcsvFailWithErrorParams when wrong parameters are entered
  testSearchcsvFailWithErrorColumnName
  testSearchcsvFailWithErrorExpression
  testSearchcsvSuccess
  testWebRequestViewcsvFileNotLoaded
  testWebRequestViewcsvAfterFileLoaded
  testWebRequestUrlNotFound when web request url is not found
  API responds to correct requests
  API responds to ill-formed requests
  Data is not assessible
