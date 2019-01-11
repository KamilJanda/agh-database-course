package main

import (
	"encoding/json"
	"fmt"
	"log"

	driver "github.com/johnnadratowski/golang-neo4j-bolt-driver"
)

const neo4jURL = "bolt://localhost:7687"

func queryToDb(query string) [][]interface{} {
	db, err := driver.NewDriver().OpenNeo(neo4jURL)
	if err != nil {
		log.Println("error connecting to neo4j:", err)
		return nil
	}
	defer db.Close()

	param := "(?i).*" + query + ".*"
	data, _, _, err := db.QueryNeoAll(query, map[string]interface{}{"query": param})
	if err != nil {
		log.Println("error querying search:", err)
		return nil
	} else if len(data) == 0 {
		log.Println("404")
		return nil
	}

	return data
}

func makeRequestToDBAndPrintResult(query string) {
	data := queryToDb(query)

	for _, result := range data {
		res := (result[0]) //.(graph.Node)).Properties
		resultJson, err := json.MarshalIndent(res, "", "  ")
		if err != nil {
			fmt.Println("error:", err)
		}
		fmt.Println(string(resultJson))
	}
}

func findActorByName(actorName string) {
	makeRequestToDBAndPrintResult(fmt.Sprintf(`
		MATCH (actor:Actor{name: "%s"}) 
		RETURN actor`, actorName))
}

func findMovieByTitleLike(movieTitle string) {
	makeRequestToDBAndPrintResult(fmt.Sprintf(`
		MATCH (movie:Movie) WHERE movie.title contains "%s" 
		RETURN movie`, movieTitle))
}

func findRatedMoviesForUser(userLogin string) {
	makeRequestToDBAndPrintResult(fmt.Sprintf(`
		MATCH (:User{name: "%s"})-[:RATED]->(movie:Movie) 
		RETURN movie`, userLogin))
}

func findCommonMoviesForActors(actorOne string, actorTwo string) {
	makeRequestToDBAndPrintResult(fmt.Sprintf(`
		MATCH (f:Actor {name: '%s'})-[:ACTS_IN]->(m)<-[:ACTS_IN]-(s: Actor {name: '%s'}) 
		RETURN m`, actorOne, actorTwo))
}

func findMovieRecommendationForUser(userLogin string) {
	makeRequestToDBAndPrintResult(fmt.Sprintf(`
		MATCH (user:Person{login: "%s"})-[:RATED]->(m:Movie)<-[:RATED]-(user2)-[:RATED]->(movies) 
		RETURN movies `, userLogin))
}

func createActorNode(actorName string) {
	makeRequestToDBAndPrintResult(fmt.Sprintf(`
		CREATE (actor:Actor {name: '%s'}) 
		RETURN actor`, actorName))
}

func createMovieNode(movieName string) {
	makeRequestToDBAndPrintResult(fmt.Sprintf(`
		CREATE (movie:Movie {title: \"%s\"}) 
		RETURN movie`, movieName))
}

func createRelationshipBetweenTwoNodes(actorName string, movieName string) {
	makeRequestToDBAndPrintResult(fmt.Sprintf(`
		MATCH (actor:Actor {name: "%s"}), (movie:Movie {title: "%s"})
		CREATE (actor)-[:ACTS_IN]->(movie) 
		RETURN actor, movie`, actorName, movieName))
}

func createActorAndMovie(actorName string, movieName string) {
	createActorNode(actorName)
	createMovieNode(movieName)
	createRelationshipBetweenTwoNodes(actorName, movieName)
}

func setActorProperties(actorName string, birthPlace string, biography string) {
	makeRequestToDBAndPrintResult(fmt.Sprintf(`
		MATCH (actor:Actor {name: "%s"}) 
		SET actor.birthplace = "%s", actor.biography = "%s" 
		RETURN actor`, actorName, birthPlace, biography))
}

func findActorsWhoActedInAtLeastSixMovies() {
	makeRequestToDBAndPrintResult(fmt.Sprintf(`
		MATCH (a: Actor)-[:ACTS_IN]->(m: Movie) WITH a, 
		collect(m) AS movies WHERE size(movies) > 5 
		RETURN a.name, size(movies)`))
}

func getAverageActedInForActorsWhoActedInAtLeastSevenMovies() {
	makeRequestToDBAndPrintResult(`
		MATCH (actor:Actor)-[:ACTS_IN]->(movie:Movie) 
		WITH actor, 
		COLLECT (DISTINCT movie.title) as movies,
		COUNT(DISTINCT actor) as actors 
		WHERE LENGTH(movies) > 6 
		RETURN SUM(LENGTH(movies)) * 1.0 / SUM(actors)`)
}

func findActorsWhoActedInAtLeastFiveMoviesAndDirectedAtLeastOne() {
	makeRequestToDBAndPrintResult(`
		MATCH (movie:Movie)<-[:ACTS_IN]-(person:Person)-[:DIRECTED]->(movie1:Movie) 
		WITH person, 
    	COLLECT(DISTINCT movie.title) as movies, COLLECT(DISTINCT movie1.title) as directedMovies
		WHERE LENGTH(movies) > 4 AND LENGTH(directedMovies) > 0 
		RETURN person.name, LENGTH(movies), LENGTH(directedMovies)
    	ORDER BY LENGTH(movies)`)
}

func findUserFriendWhoRatedMovieAtLeastThreeStars(userLogin string) {
	makeRequestToDBAndPrintResult(fmt.Sprintf(`
		MATCH (user)-[:FRIEND]->(friend)-[rated:RATED]->(movie:Movie) 
		WHERE user.name = "%s"
		AND rated.stars > 2 
		RETURN friend, movie.title, rated.stars`, userLogin))
}

func findPathBetweenTwoActors(actorOne string, actorTwo string) {
	makeRequestToDBAndPrintResult(fmt.Sprintf(`
		MATCH p=shortestPath((a:Actor {name: '%s'})-[*]-(a1:Actor {name: '%s'}))
		RETURN extract(n in filter(x in nodes(p) where (x:Actor)) | n.name) as path`, actorOne, actorTwo))
}

func indexTest(actorOne string, actorTwo string) {
	fmt.Println("Without index:")
	dropIndex()
	createSearchingWithIndex(actorOne)
	createShortestPathBetweenActors(actorOne, actorTwo)

	fmt.Println("With index:")
	createIndex()
	createSearchingWithIndex(actorOne)
	createShortestPathBetweenActors(actorOne, actorTwo)
}

func createSearchingWithIndex(actorName string) {
	makeRequestToDBAndPrintResult(fmt.Sprintf(`
		PROFILE MATCH (a: Actor) 
		USING INDEX a:Actor(name) 
		WHERE a.name = '%s' return a`, actorName))
}

func createShortestPathBetweenActors(actorOne string, actorTwo string) {
	makeRequestToDBAndPrintResult(fmt.Sprintf(`
		PROFILE MATCH (a: Actor {name: '%s'}), (b: Actor {name: '%s'}) 
		RETURN shortestPath((a)-[*]-(b)) as shortest_path`, actorOne, actorTwo))
}

func dropIndex() {
	makeRequestToDBAndPrintResult(fmt.Sprintf(`DROP INDEX ON :Actor(name)`))
}

func createIndex() {
	makeRequestToDBAndPrintResult(fmt.Sprintf(`CREATE INDEX ON :Actor(name)`))
}

func main() {
	//ex 3
	fmt.Println("\nex 3")
	findActorByName("Tommy Wiseau")
	findMovieByTitleLike("The Room")
	findRatedMoviesForUser("maheshksp")
	findCommonMoviesForActors("Harrison Ford", "Carrie Fisher")
	findMovieRecommendationForUser("Sangreal")

	//ex4
	fmt.Println("\nex 4")
	createActorAndMovie("Reincarnation of Tommy Wiseau", "The Room 2")

	//ex5
	fmt.Println("\nex 5")
	setActorProperties("Reincarnation of Tommy Wiseau", "New York", "long story")

	//ex6
	fmt.Println("\nex 6")
	findActorsWhoActedInAtLeastSixMovies()

	//ex7
	fmt.Println("\nex 7")
	getAverageActedInForActorsWhoActedInAtLeastSevenMovies()

	//ex8
	fmt.Println("\nex 8")
	findActorsWhoActedInAtLeastFiveMoviesAndDirectedAtLeastOne()

	//ex9
	fmt.Println("\nex 9")
	findUserFriendWhoRatedMovieAtLeastThreeStars("maheshksp")

	//ex10
	fmt.Println("\nex 10")
	findPathBetweenTwoActors("Harrison Ford", "Carrie Fisher")

	fmt.Println("\nex 11")
	indexTest("Harrison Ford", "Carrie Fisher")
}
