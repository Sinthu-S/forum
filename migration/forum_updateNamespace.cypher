begin transaction
match (a:Action) where a.name='fr.wseduc.forum.controllers.ForumController|createMessage'
set a.name='net.atos.entng.forum.controllers.ForumController|createMessage';
match (a:Action) where a.name='fr.wseduc.forum.controllers.ForumController|createSubject'
set a.name='net.atos.entng.forum.controllers.ForumController|createSubject';
match (a:Action) where a.name='fr.wseduc.forum.controllers.ForumController|updateCategory'
set a.name='net.atos.entng.forum.controllers.ForumController|updateCategory';
match (a:Action) where a.name='fr.wseduc.forum.controllers.ForumController|shareCategory'
set a.name='net.atos.entng.forum.controllers.ForumController|shareCategory';
match (a:Action) where a.name='fr.wseduc.forum.controllers.ForumController|createCategory'
set a.name='net.atos.entng.forum.controllers.ForumController|createCategory';
commit