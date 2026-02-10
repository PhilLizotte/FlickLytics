function buttonPrompt()
{
    // IMPORTANT: To get this to work yourself, you may need
    //   to use your TMDb API Read Access Token (found in your
    //   TMDb account > "API subscription" page)

    // View the following page to generate your own requests:
    //   https://developer.themoviedb.org/reference/movie-popular-list

    const url = 'https://api.themoviedb.org/3/movie/popular?language=en-US&page=1';
    const options = {
        method: 'GET',
        headers: {
            accept: 'application/json',
            Authorization: 'Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIyZjhkNzFmYjkxMzY3ZTQxMDI4NDc0NzVmNjY2YWEyNSIsIm5iZiI6MTc2OTk2MzY0My4xNzYsInN1YiI6IjY5N2Y4MDdiZTA2MzEzNTVkYTZmNzY1YiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.dUsuwgFuTaQIz_X2UcPpMOchaPIhELZ7UyP7I4SKt7c'
        }
    };

    fetch(url, options)
        .then(res => res.json())
        .then(json => {
            console.log(json)

            let text = "Title: <i>";
            text += json.results[0].original_title
            text += "</i>, Language: <i>"
            text += json.results[0].original_language
            text += "</i>, Average vote score: <i>"
            text += json.results[0].vote_average
            text += "</i>"

            let par = document.getElementById("textGoesHere");
            par.innerHTML = text;
        })
        .catch(err => console.error(err));


}