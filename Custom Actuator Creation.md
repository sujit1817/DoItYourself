How to create a custome actuator??

1.Add dependency
spring-boot-starter-actuator

2.Enable actuator
In application.properties
management.endpoints.web.exposure.include=*

Note: In prod, never expose all endpoints (*), only required ones

3.Create custom actuator
Spring provide annotation

    @Endpoint


example

    @Component
    @Endpoint(id = "sujitAppInfo")
    public class CustomActuatorEndpoint {
    
    	@ReadOperation
    	public Map<String, Object> getCustomInfo() {
    		Map<String, Object> data = new HashMap<>();
    		data.put("status", "Application is running fine");
    		data.put("version","1.0");
    	}
    }

Step 4: Run and Test

start you application and hit endpoint
http://localhost:8080/actuator/sujitAppInfo

output:
JSON

{
	"status": "Application is running fine",
	"version": "1.0"
}


Types of operations
1.Read Operation(GET)
@ReadOperation

2.Write Operation(POST)
@WriteOperation
public String updateData(String name) {
	return "updated " +name;
}

3.Delete Operation(DELETE)
@DeleteOperation


Step 5 : Secure actuator
add spring security
management.endpoint.web.exposure.include=health,info,loggers,sujitAppInfo
management.endpoint.health.show-details=always

 Advanced
    To create a custom health indicator

    implement HealthIndicator
    @Override health method


   flow - >
    spring boot starts
    actuator auto-configuration loads
    it scans: @Endpoint and @HealthIndicator
    Registers them as beans
    maps them under /actuator/*

| Use Case                   | Solution            |
| -------------------------- | ------------------- |
| Custom monitoring endpoint | `@Endpoint`         |
| Add to health status       | `HealthIndicator`   |
| Metrics                    | Micrometer          |
| Logs                       | `/actuator/loggers` |
