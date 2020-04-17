package net.mixednutz.app.server.extensions.hibernate;

import java.sql.Types;

import org.hibernate.dialect.HSQLDialect;

public class CustomHsqlDialect extends HSQLDialect {

	public CustomHsqlDialect() {
		super();
		registerColumnType(Types.CLOB, "clob");
	}

}
