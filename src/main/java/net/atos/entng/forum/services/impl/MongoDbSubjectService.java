/*
 * Copyright © Région Nord Pas de Calais-Picardie,  Département 91, Région Aquitaine-Limousin-Poitou-Charentes, 2016.
 *
 * This file is part of OPEN ENT NG. OPEN ENT NG is a versatile ENT Project based on the JVM and ENT Core Project.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License).
 *
 * For the sake of explanation, any module that communicate over native
 * Web protocols, such as HTTP, with OPEN ENT NG is outside the scope of this
 * license and could be license under its own terms. This is merely considered
 * normal use of OPEN ENT NG, and does not fall under the heading of "covered work".
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package net.atos.entng.forum.services.impl;

import static org.entcore.common.mongodb.MongoDbResult.validActionResultHandler;
import static org.entcore.common.mongodb.MongoDbResult.validResultHandler;
import static org.entcore.common.mongodb.MongoDbResult.validResultsHandler;

import net.atos.entng.forum.services.SubjectService;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import com.mongodb.QueryBuilder;

import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.mongodb.MongoQueryBuilder;
import fr.wseduc.mongodb.MongoUpdateBuilder;
import fr.wseduc.webutils.Either;
import org.vertx.java.core.json.impl.Json;

public class MongoDbSubjectService extends AbstractService implements SubjectService {

	public MongoDbSubjectService(final String categories_collection, final String subjects_collection) {
		super(categories_collection, subjects_collection);
	}

	@Override
	public void list(final String categoryId, final UserInfos user, final Handler<Either<String, JsonArray>> handler) {
		// Query
			QueryBuilder query = QueryBuilder.start("category").is(categoryId);
		JsonObject sort = new JsonObject().putNumber("modified", -1);

		// Projection
		JsonObject projection = new JsonObject();
		JsonObject slice = new JsonObject();
		slice.putNumber("$slice", -1);
		projection.putObject("messages", slice);

		mongo.find(subjects_collection, MongoQueryBuilder.build(query), sort, projection, validResultsHandler(handler));
	}

	@Override
	public void listPlus(String[] categoryIdArray, UserInfos user, Handler<Either<String, JsonArray>> handler) {
		QueryBuilder query = QueryBuilder.start("category").in(categoryIdArray);
		JsonObject sort = new JsonObject().putNumber("modified", -1);

		JsonObject projection = new JsonObject();
		JsonObject slice = new JsonObject();
		slice.putNumber("$slice", -1);
		projection.putObject("message", slice);

		mongo.find(subjects_collection, MongoQueryBuilder.build(query), sort, projection, validResultsHandler(handler));
	}

	@Override
	public void create(String categoryId, JsonObject body, UserInfos user, Handler<Either<String, JsonObject>> handler) {

		// Clean data
		body.removeField("_id");
		body.removeField("category");
		body.removeField("messages");

		// Prepare data
		JsonObject now = MongoDb.now();
		body.putObject("owner", new JsonObject()
				.putString("userId", user.getUserId())
				.putString("displayName", user.getUsername())
		).putObject("created", now).putObject("modified", now)
		.putString("category", categoryId);

		mongo.save(subjects_collection, body, validActionResultHandler(handler));

	}

	@Override
	public void retrieve(String categoryId, String subjectId, UserInfos user, Handler<Either<String, JsonObject>> handler) {
		// Query
		QueryBuilder query = QueryBuilder.start("_id").is(subjectId);
		query.put("category").is(categoryId);

		// Projection
		JsonObject projection = new JsonObject();
		JsonObject slice = new JsonObject();
		slice.putNumber("$slice", -1);
		projection.putObject("messages", slice);

		mongo.findOne(subjects_collection,  MongoQueryBuilder.build(query), projection, validResultHandler(handler));
	}

	@Override
	public void update(String categoryId, String subjectId, JsonObject body, UserInfos user, Handler<Either<String, JsonObject>> handler) {
		// Query
		QueryBuilder query = QueryBuilder.start("_id").is(subjectId);
		query.put("category").is(categoryId);

		// Clean data
		body.removeField("_id");
		body.removeField("category");
		body.removeField("messages");

		// Modifier
		MongoUpdateBuilder modifier = new MongoUpdateBuilder();
		for (String attr: body.getFieldNames()) {
			modifier.set(attr, body.getValue(attr));
		}
		modifier.set("modified", MongoDb.now());
		mongo.update(subjects_collection, MongoQueryBuilder.build(query), modifier.build(), validActionResultHandler(handler));
	}

	@Override
	public void delete(String categoryId, String subjectId, UserInfos user, Handler<Either<String, JsonObject>> handler) {
		QueryBuilder query = QueryBuilder.start("_id").is(subjectId);
		query.put("category").is(categoryId);
		mongo.delete(subjects_collection, MongoQueryBuilder.build(query), validActionResultHandler(handler));
	}

	@Override
	public void getSubjectTitle(String categoryId, String subjectId, UserInfos user, Handler<Either<String, JsonObject>> handler) {
		QueryBuilder query = QueryBuilder.start("_id").is(subjectId);
		query.put("category").is(categoryId);
		// Projection
		JsonObject projection = new JsonObject();
		projection.putNumber("title", 1);
		mongo.findOne(subjects_collection, MongoQueryBuilder.build(query), projection, validActionResultHandler(handler));
	}


}
