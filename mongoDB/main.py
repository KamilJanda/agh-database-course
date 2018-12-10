from pymongo import MongoClient
from bson.code import Code

myclient = MongoClient("mongodb://localhost:27017/")

db = myclient["jeopardy"]

ex1 = db.question \
    .find({"round": "Jeopardy!", "category": "HISTORY"}, {"question": 1, "answer": 1}) \
    .sort("show_number", -1)

print("ex1 --------------------------------")

for x in ex1:
    print(x)

ex2 = db.question \
    .aggregate([
    {"$match": {"category": "HISTORY", "round": "Double Jeopardy!"}},
    {"$group": {"_id": "$value", "total": {"$sum": 1}}},
    {"$sort": {"total": -1}}])

print("ex2 --------------------------------")

for x in ex2:
    print(x)

mapper = Code("""function(){
  	if(this.value != null)	{
  	  var parsedValue = parseInt(this.value.substring(1))
  	  emit(this.category, parsedValue)
  	} else {
  		emit(this.category, 0)
  	}
}
""")

reducer = Code("""function(category,value){
  return Array.sum(value)
}""")

ex3 = db.question \
    .map_reduce(mapper, reducer, "total_value_of_questions", query={"round": "Jeopardy!"}, limit=20)

print("ex3 --------------------------------")
for x in ex3.find():
    print(x)
