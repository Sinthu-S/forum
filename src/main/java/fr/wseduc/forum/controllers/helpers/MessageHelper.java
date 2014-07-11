package fr.wseduc.forum.controllers.helpers;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.forum.services.MessageService;

public class MessageHelper extends ExtractorHelper {

	private static final String CATEGORY_ID_PARAMETER = "id";
	private static final String SUBJECT_ID_PARAMETER = "subjectid";
	private static final String MESSAGE_ID_PARAMETER = "messageid";
	
	private final MessageService messageService;
	
	public MessageHelper(final MessageService messageService) {
		this.messageService = messageService;
	}
	
	public void list(final HttpServerRequest request) {
		final String categoryId = extractParameter(request, CATEGORY_ID_PARAMETER);
		final String subjectId = extractParameter(request, SUBJECT_ID_PARAMETER);
		if (categoryId == null || subjectId == null) {
			return;
		}
		
		extractUserFromRequest(request, new Handler<UserInfos>(){
			@Override
			public void handle(final UserInfos user) {
				try {
					messageService.list(categoryId, subjectId, user, arrayResponseHandler(request));
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
		final String messageId = extractParameter(request, MESSAGE_ID_PARAMETER);
		if (categoryId == null || subjectId == null || messageId == null) {
			return;
		}
		
		extractUserFromRequest(request, new Handler<UserInfos>(){
			@Override
			public void handle(final UserInfos user) {
				try {
					messageService.retrieve(categoryId, subjectId, messageId, user, notEmptyResponseHandler(request));
				}
				catch (Exception e) {
					renderErrorException(request, e);
				}
			}
		});
	}
	
	public void create(final HttpServerRequest request) {
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
							messageService.create(categoryId, subjectId, body, user, notEmptyResponseHandler(request));
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
		final String messageId = extractParameter(request, MESSAGE_ID_PARAMETER);
		if (categoryId == null || subjectId == null || messageId == null) {
			return;
		}
		
		extractUserFromRequest(request, new Handler<UserInfos>(){
			@Override
			public void handle(final UserInfos user) {
				extractBodyFromRequest(request, new Handler<JsonObject>(){
					@Override
					public void handle(final JsonObject body) {
						try {
							messageService.update(categoryId, subjectId, messageId, body, user, notEmptyResponseHandler(request));
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
		final String messageId = extractParameter(request, MESSAGE_ID_PARAMETER);
		if (categoryId == null || subjectId == null || messageId == null) {
			return;
		}
		
		extractUserFromRequest(request, new Handler<UserInfos>(){
			@Override
			public void handle(final UserInfos user) {
				try {
					messageService.delete(categoryId, subjectId, messageId, user, notEmptyResponseHandler(request));
				}
				catch (Exception e) {
					renderErrorException(request, e);
				}
			}
		});
	}
}
