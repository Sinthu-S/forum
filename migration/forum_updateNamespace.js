db.forum.categories.find({"shared.0" : { "$exists" : true }}, {"_id":1, "shared":1}).forEach(function(doc) {
  var string = JSON.stringify(doc);
  var obj = JSON.parse(string.replace(/fr\-wseduc/g, 'net-atos-entng'));
  db.forum.categories.update({"_id" : doc._id}, { $set : { "shared" : obj.shared}});
});
db.forum.subjects.find({"shared.0" : { "$exists" : true }}, {"_id":1, "shared":1}).forEach(function(doc) {
  var string = JSON.stringify(doc);
  var obj = JSON.parse(string.replace(/fr\-wseduc/g, 'net-atos-entng'));
  db.forum.subjects.update({"_id" : doc._id}, { $set : { "shared" : obj.shared}});
});