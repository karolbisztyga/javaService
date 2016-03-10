package bb.service.database;

import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import bb.service.database.entities.UserEntity;

public class UpdateDatabase {

	public static void main(String[] args) {
		AnnotationConfiguration config = new AnnotationConfiguration();
        config.addAnnotatedClass(UserEntity.class);
        config.configure("hibernate.cfg.xml");
        new SchemaExport(config).create(true, true);
	}

}
