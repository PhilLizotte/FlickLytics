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

    const url =
        "/api/search?category=" +
        encodeURIComponent(category) +
        "&query=" +
        encodeURIComponent(query);

    fetch(url)
        .then((res) => res.json())
        .then((json) => {
            const block = document.createElement("div");
            const header = document.createElement("div");
            header.innerText = 'Search: "' + query + '" (' + category + ")";
            block.appendChild(header);

            const list = document.createElement("ol");
            const items = json && json.results ? json.results.slice(0, 10) : [];
            for (let i = 0; i < items.length; i++) {
                const li = document.createElement("li");
                const item = items[i] || {};

                // DEBUG:: display the items in the console.
                // console.log(item);

                if (category === "person") {
                    const name = item.name || "(no name)";

                    const idText = document.createElement("span");
                    idText.innerText =
                        "ID: " +
                        (item.id !== undefined && item.id !== null ?
                            item.id
                        :   "") +
                        ", ";
                    li.appendChild(idText);

                    const nameSpan = document.createElement("span");
                    nameSpan.innerText = name + ", ";
                    li.appendChild(nameSpan);

                    const popularity =
                        (
                            item.popularity !== undefined &&
                            item.popularity !== null
                        ) ?
                            item.popularity
                        :   "";
                    const popSpan = document.createElement("span");
                    popSpan.innerText = "Popularity: " + popularity + ", ";
                    li.appendChild(popSpan);

                    const photoUrl = item.photoUrl || "";
                    if (photoUrl) {
                        const photoLink = document.createElement("a");
                        photoLink.href = photoUrl;
                        photoLink.target = "_blank";
                        photoLink.rel = "noopener noreferrer";
                        photoLink.innerText = "[Photo] ";
                        li.appendChild(photoLink);
                    }

                    const gender =
                        item.gender === 1 ? "Female"
                        : item.gender === 2 ? "Male"
                        : "Other/Unknown";
                    const dept = item.known_for_department || "";

                    const infoSpan = document.createElement("span");
                    infoSpan.innerText =
                        "Gender: " + gender + ", Department: " + dept + ", ";
                    li.appendChild(infoSpan);

                    const personId =
                        item.id !== undefined && item.id !== null ?
                            item.id
                        :   "";
                    const knownForUrl =
                        personId ?
                            "/person/" +
                            encodeURIComponent(personId) +
                            "/known-for"
                        :   "";
                    if (knownForUrl) {
                        const knownForLink = document.createElement("a");
                        knownForLink.href = knownForUrl;
                        knownForLink.target = "_blank";
                        knownForLink.rel = "noopener noreferrer";
                        knownForLink.innerText = "Known for";
                        li.appendChild(knownForLink);
                    }
                } else {
                    const title =
                        item.title ||
                        item.name ||
                        item.original_name ||
                        "(no title)";

                    const idText = document.createElement("span");
                    idText.innerText =
                        "ID: " +
                        (item.id !== undefined && item.id !== null ?
                            item.id
                        :   "") +
                        ", ";
                    li.appendChild(idText);

                    const titleLink = document.createElement("a");
                    titleLink.href = item.detailsUrl || "";
                    titleLink.target = "_blank";
                    titleLink.rel = "noopener noreferrer";
                    titleLink.innerText = title;
                    li.appendChild(titleLink);

                    const language =
                        item.language || item.original_language || "";
                    const genres =
                        Array.isArray(item.genres) ?
                            item.genres.join(", ")
                        :   "";
                    const releaseDate =
                        item.releaseDate ||
                        item.release_date ||
                        item.first_air_date ||
                        "";
                    const popularity =
                        (
                            item.popularity !== undefined &&
                            item.popularity !== null
                        ) ?
                            item.popularity
                        :   "";
                    const voteAverage =
                        (
                            item.vote_average !== undefined &&
                            item.vote_average !== null
                        ) ?
                            item.vote_average
                        :   "";
                    const reviewsSentiment =
                        (
                            item.reviewsSentiment !== undefined &&
                            item.reviewsSentiment !== null
                        ) ?
                            item.reviewsSentiment
                        :   "";

                    const meta = document.createElement("span");

                    // Give this HTML element *a unique ID so I can reference it in the movie detail call
                    meta.innerText =
                        ", Language: " +
                        language +
                        ", Genres: " +
                        genres +
                        ", Release Date: " +
                        releaseDate +
                        ", Popularity: " +
                        popularity +
                        ", Vote Average: " +
                        voteAverage +
                        ", Reviews sentiment: " +
                        reviewsSentiment;
                    li.appendChild(meta);

                    // Financial Performance feature integration
                    if (category === "movie") {
                    // Generate link to financial information page
                        const fpLinkSpan = document.createElement("span");
                        const financeLink = document.createElement("a");
                        financeLink.href = "/finances/" + item.id;
                        financeLink.innerText =
                            "View financial performance";
                        fpLinkSpan.appendChild(financeLink);
                        li.appendChild(document.createElement("br"));
                        li.appendChild(fpLinkSpan);
                    }

                    const singlePageUrl = document.createElement("a");
                    singlePageUrl.innerText = ", link to this item";
                    singlePageUrl.href =
                        "tv" === category ?
                            "/tv/" + item.id
                        :   "/movie/" + item.id;
                    li.appendChild(document.createElement("br"));
                    li.appendChild(singlePageUrl);
                }
                list.appendChild(li);
            }
            block.appendChild(list);

            resultsContainer.prepend(block);

            while (resultsContainer.children.length > 10) {
                resultsContainer.removeChild(resultsContainer.lastElementChild);
            }
        })
        .catch((err) => console.error(err));
}
