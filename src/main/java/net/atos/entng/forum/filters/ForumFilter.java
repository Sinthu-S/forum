package net.atos.entng.forum.filters;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

public interface ForumFilter {

	public void messageMine(final HttpServerRequest request, final String sharedMethod, final UserInfos user, final Handler<Boolean> handler);
}
