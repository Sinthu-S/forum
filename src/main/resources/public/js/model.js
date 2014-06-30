function Category(){

}

function Thread(){

}

function Message(){

}

model.build = function(){
	this.makeModels([Category, Thread, Message]);
}