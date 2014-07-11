package fr.wseduc.forum.filters.impl;

import org.entcore.common.http.filter.MongoAppFilter;

import fr.wseduc.forum.filters.ForumFilter;
import fr.wseduc.forum.services.MessageService;

public class ForumResourceFilter extends MongoAppFilter implements ForumFilter {

	private final MessageService messageService;
	
	public ForumResourceFilter(final String collection, final MessageService messageService) {
		super(collection);
		this.messageService = messageService;
	}

}
