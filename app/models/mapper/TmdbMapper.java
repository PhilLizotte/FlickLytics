package models.mapper;

import models.domain.Movie;
import models.domain.TVShow;
import models.dto.MovieDTO;
import models.dto.TVShowDTO;

/**
 * This is domain model for representing Movie.
 * The fields are based on the Appendix of the Project Document.
 *
 * @author  Ali Maher
 */

public class TmdbMapper {
    public static Movie toMovie(MovieDTO movieDTO) {
        return new Movie(
                movieDTO.id,
                movieDTO.title,
                movieDTO.overview,
                movieDTO.release_date,
                movieDTO.genres,
                movieDTO.homepage,
                movieDTO.popularity,
                movieDTO.poster_path,
                movieDTO.production_companies,
                movieDTO.revenue,
                movieDTO.runtime,
                movieDTO.spoken_languages,
                movieDTO.status,
                movieDTO.tagline,
                movieDTO.vote_average,
                movieDTO.vote_count
        );
    }

    public static TVShow toTVShow(TVShowDTO tvShowDTO) {
        return new TVShow(
                tvShowDTO.id,
                tvShowDTO.name,
                tvShowDTO.overview,
                tvShowDTO.first_air_date,
                tvShowDTO.last_air_date,
                tvShowDTO.popularity,
                tvShowDTO.poster_path,
                tvShowDTO.genres,
                tvShowDTO.homepage,
                tvShowDTO.number_of_episodes,
                tvShowDTO.number_of_seasons,
                tvShowDTO.networks,
                tvShowDTO.status,
                tvShowDTO.tagline,
                tvShowDTO.vote_average,
                tvShowDTO.vote_count,
                tvShowDTO.type
        );
    }
}
