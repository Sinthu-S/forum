package fr.wseduc.forum.filters.impl;

import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.mongodb.MongoDbConf;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

import fr.wseduc.forum.Forum;
import fr.wseduc.forum.services.MessageService;
import fr.wseduc.forum.services.impl.MongoDbMessageService;
import fr.wseduc.webutils.http.Binding;

public class ForumMessageMine implements ResourcesProvider {

	private final MessageService messageService;
	
	private static final String CATEGORY_ID_PARAMETER = "id";
	private static final String SUBJECT_ID_PARAMETER = "subjectid";
	private static final String MESSAGE_ID_PARAMETER = "messageid";
	
	private MongoDbConf conf = MongoDbConf.getInstance();
	
	public ForumMessageMine() {
		this.messageService = new MongoDbMessageService(Forum.CATEGORY_COLLECTION, Forum.SUBJECT_COLLECTION);
	}

	@Override
	public void authorize(final HttpServerRequest request, final Binding binding, final UserInfos user, final Handler<Boolean> handler) {
		final String categoryId = request.params().get(CATEGORY_ID_PARAMETER);
		final String subjectId = request.params().get(SUBJECT_ID_PARAMETER);
		final String messageId = request.params().get(MESSAGE_ID_PARAMETER);
		
		final String sharedMethod = binding.getServiceMethod().replaceAll("\\.", "-");
		
		if (categoryId == null || categoryId.trim().isEmpty()
				|| subjectId == null || subjectId.trim().isEmpty()
				|| messageId == null || messageId.trim().isEmpty()) {
			handler.handle(false);
			return;
		}
		
		request.pause();
		messageService.checkIsMine(categoryId, subjectId, messageId, user, sharedMethod, new Handler<Boolean>(){
			@Override
			public void handle(Boolean event) {
				request.resume();
				handler.handle(event);
			}
		});
	}
}
