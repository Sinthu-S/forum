package fr.wseduc.forum.controllers.helpers;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.forum.services.SubjectService;

public class SubjectHelper extends ExtractorHelper {

	private final SubjectService subjectService;
	
	private static final String CATEGORY_ID_PARAMETER = "id";
	private static final String SUBJECT_ID_PARAMETER = "subjectid";
	
	public SubjectHelper(final SubjectService subjectService) {
		this.subjectService = subjectService;
	}
	
	public void list(final HttpServerRequest request) {
		final String categoryId = extractParameter(request, CATEGORY_ID_PARAMETER);
		if (categoryId == null) {
			return;
		}
		
		extractUserFromRequest(request, new Handler<UserInfos>(){
			@Override
			public void handle(final UserInfos user) {
				try {
					subjectService.list(categoryId, user, arrayResponseHandler(request));
				}
				catch (Exception e) {
					renderErrorException(request, e);
				}
			}
		});
	}
	
	public void retrieve(final HttpServerRequest request) {
		final String categoryId = extractParameter(request, CATEGORY_ID_PARAMETER);
		final String subjectId = extractParameter(request, SUBJECT_ID_PARAMETER);
		if (categoryId == null || subjectId == null) {
			return;
		}
		
		extractUserFromRequest(request, new Handler<UserInfos>(){
			@Override
			public void handle(final UserInfos user) {
				try {
					subjectService.retrieve(categoryId, subjectId, user, notEmptyResponseHandler(request));
				}
				catch (Exception e) {
					renderErrorException(request, e);
				}
			}
		});
	}
	
	public void create(final HttpServerRequest request) {
		final String categoryId = extractParameter(request, CATEGORY_ID_PARAMETER);
		if (categoryId == null) {
			return;
		}
		
		extractUserFromRequest(request, new Handler<UserInfos>(){
			@Override
			public void handle(final UserInfos user) {
				extractBodyFromRequest(request, new Handler<JsonObject>(){
					@Override
					public void handle(final JsonObject body) {
						try {
							subjectService.create(categoryId, body, user, notEmptyResponseHandler(request));
						}
						catch (Exception e) {
							renderErrorException(request, e);
						}
					}
				});
				
			}
		});
	}
	
	public void update(final HttpServerRequest request) {
		final String categoryId = extractParameter(request, CATEGORY_ID_PARAMETER);
		final String subjectId = extractParameter(request, SUBJECT_ID_PARAMETER);
		if (categoryId == null || subjectId == null) {
			return;
		}
		
		extractUserFromRequest(request, new Handler<UserInfos>(){
			@Override
			public void handle(final UserInfos user) {
				extractBodyFromRequest(request, new Handler<JsonObject>(){
					@Override
					public void handle(final JsonObject body) {
						try {
							subjectService.update(categoryId, subjectId, body, user, notEmptyResponseHandler(request));
						}
						catch (Exception e) {
							renderErrorException(request, e);
						}
					}
				});
				
			}
		});
	}
	
	public void delete(final HttpServerRequest request) {
		final String categoryId = extractParameter(request, CATEGORY_ID_PARAMETER);
		final String subjectId = extractParameter(request, SUBJECT_ID_PARAMETER);
		if (categoryId == null || subjectId == null) {
			return;
		}
		
		extractUserFromRequest(request, new Handler<UserInfos>(){
			@Override
			public void handle(final UserInfos user) {
				try {
					subjectService.delete(categoryId, subjectId, user, notEmptyResponseHandler(request));
				}
				catch (Exception e) {
					renderErrorException(request, e);
				}
			}
		});
	}
}
