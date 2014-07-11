package fr.wseduc.forum.filters.impl;

import org.entcore.common.http.filter.MongoAppFilter;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

import fr.wseduc.forum.filters.ForumFilter;
import fr.wseduc.forum.services.MessageService;

public class ForumResourceFilter extends MongoAppFilter implements ForumFilter {

	private final MessageService messageService;
	
	private static final String CATEGORY_ID_PARAMETER = "id";
	private static final String SUBJECT_ID_PARAMETER = "subjectid";
	private static final String MESSAGE_ID_PARAMETER = "messageid";
	
	public ForumResourceFilter(final String collection, final MessageService messageService) {
		super(collection);
		this.messageService = messageService;
	}
	
	@Override
	public void messageMine(final HttpServerRequest request, final String sharedMethod, final UserInfos user, final Handler<Boolean> handler) {
		final String categoryId = request.params().get(CATEGORY_ID_PARAMETER);
		final String subjectId = request.params().get(SUBJECT_ID_PARAMETER);
		final String messageId = request.params().get(MESSAGE_ID_PARAMETER);
		
		if (categoryId == null || categoryId.trim().isEmpty()
				|| subjectId == null || subjectId.trim().isEmpty()
				|| messageId == null || messageId.trim().isEmpty()) {
			handler.handle(false);
			return;
		}
		
		messageService.checkIsMine(categoryId, subjectId, messageId, user, sharedMethod, handler);
	}

}
