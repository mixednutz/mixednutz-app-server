package net.mixednutz.app.server.controller.api;

import net.mixednutz.api.core.model.PageRequest;
import net.mixednutz.app.server.controller.exception.BadParametersException;

public class PaginationSupport {

	public static void checkValidPagination(PageRequest<?> prevPage) {
		if (prevPage!=null && prevPage.getStart()==null && prevPage.getEnd()==null) {
			throw new BadParametersException("Both start and end cannot be null");
		}
	}
	
}
