package org.mariotaku.querybuilder.query;

import org.mariotaku.querybuilder.SQLLang;

public class SQLDropTableQuery implements SQLLang {

	private final boolean dropIfExists;
	private final String table;

	public SQLDropTableQuery(final boolean dropIfExists, final String table) {
		if (table == null) throw new NullPointerException();
		this.dropIfExists = dropIfExists;
		this.table = table;
	}

	@Override
	public String getSQL() {
		if (dropIfExists) return String.format("DROP TABLE IF EXISTS %s", table);
		return String.format("DROP TABLE %s", table);
	}

}
