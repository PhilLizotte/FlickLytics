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

function performSearch() {
    const queryInput = document.getElementById("searchQuery");
    const categorySelect = document.getElementById("searchCategory");
    const resultsContainer = document.getElementById("resultsContainer");

    if (!queryInput || !categorySelect || !resultsContainer) {
        return;
    }

    const query = (queryInput.value || "").trim();
    const category = (categorySelect.value || "").trim();

    if (!query || !category) {
        return;
    }

    const url = "/api/search?category=" + encodeURIComponent(category) + "&query=" + encodeURIComponent(query);

    fetch(url)
        .then(res => res.json())
        .then(json => {
            const block = document.createElement("div");
            const header = document.createElement("div");
            header.innerText = "Search: \"" + query + "\" (" + category + ")";
            block.appendChild(header);

            const list = document.createElement("ol");
            const items = (json && json.results) ? json.results.slice(0, 10) : [];
            for (let i = 0; i < items.length; i++) {
                const li = document.createElement("li");
                const item = items[i] || {};
                li.innerText = item.title || item.name || item.original_name || "(no title)";
                list.appendChild(li);
            }
            block.appendChild(list);

            resultsContainer.prepend(block);

            while (resultsContainer.children.length > 10) {
                resultsContainer.removeChild(resultsContainer.lastElementChild);
            }
        })
        .catch(err => console.error(err));
}