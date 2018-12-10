db.question
    .find({round: "Jeopardy!", category: "HISTORY"}, {question: 1, answer: 1})
    .sort(show_number, -1);

db.question
    .aggregate([
        {$match: {category: "HISTORY", round: "Double Jeopardy!"}},
        {$group: {_id: "$value", total: {$sum: 1}}},
        {$sort: {total: -1}}]);


db.question.mapReduce(
    function () {
        if (this.value != null) {
            var parsedValue = parseInt(this.value.substring(1));
            emit(this.category, parsedValue)
        } else {
            emit(this.category, 0)
        }
    },
    function (category, value) {
        return Array.sum(value)
    }, {
        query: {round: "Jeopardy!"},
        out: "total_value_of_questions",
        limit: 20
    });

db.total_value_of_questions.find();

