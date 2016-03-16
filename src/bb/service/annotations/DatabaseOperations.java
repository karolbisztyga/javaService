package bb.service.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface DatabaseOperations {
	//ta adnotacja ma byc przed kazda metoda w usermanager i teraz w manageraspect robimy porade around po tej adnotacji, 
	//ktora ma wylapywac hibrnate exceptio i go obslugiwac!!
}
