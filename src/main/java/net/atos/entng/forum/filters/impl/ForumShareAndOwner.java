package net.atos.entng.forum.filters.impl;


import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import fr.wseduc.mongodb.MongoQueryBuilder;
import fr.wseduc.webutils.http.Binding;
import org.entcore.common.http.filter.MongoAppFilter;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.mongodb.MongoDbConf;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

import java.util.ArrayList;
import java.util.List;

public class ForumShareAndOwner implements ResourcesProvider {

    private MongoDbConf conf = MongoDbConf.getInstance();

    @Override
    public void authorize(HttpServerRequest request, Binding binding, UserInfos user, Handler<Boolean> handler) {
        List<String> idList = request.params().getAll("id");
        if (idList != null && !idList.isEmpty()) {
            List<DBObject> groups = new ArrayList<>();
            String sharedMethod = binding.getServiceMethod().replaceAll("\\.", "-");
            groups.add(QueryBuilder.start("userId").is(user.getUserId())
                    .put(sharedMethod).is(true).get());
            for (String gpId: user.getGroupsIds()) {
                groups.add(QueryBuilder.start("groupId").is(gpId)
                        .put(sharedMethod).is(true).get());
            }
            QueryBuilder query = QueryBuilder.start("_id").in(idList).or(
                    QueryBuilder.start("owner.userId").is(user.getUserId()).get(),
                    QueryBuilder.start("shared").elemMatch(
                            new QueryBuilder().or(groups.toArray(new DBObject[groups.size()])).get()).get()
            );
            MongoAppFilter.executeCountQuery(request, conf.getCollection(), MongoQueryBuilder.build(query), idList.size(), handler);
        } else {
            handler.handle(false);
        }
    }
}
