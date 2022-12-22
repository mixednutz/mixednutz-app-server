package net.mixednutz.app.server.manager.impl;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbFind;
import info.movito.themoviedbapi.TmdbFind.ExternalSource;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.TmdbMovies.MovieMethod;
import info.movito.themoviedbapi.model.FindResults;
import info.movito.themoviedbapi.model.MovieDb;
import net.mixednutz.app.server.manager.ExternalContentManager.ExtractedMetadata;
import net.mixednutz.app.server.manager.impl.ExternalContentManagerImpl.ApiLookup;

@Component
@ConditionalOnProperty("themoviedb.apikey")
public class ThemoviedbApi implements ApiLookup<ExtractedMetadata>  {
	
	private static final String URL = 
			"https://www.themoviedb.org/movie/{id}";
	private static final String IMAGE_URL_PREFIX = 
			"https://www.themoviedb.org/t/p/w600_and_h900_bestv2";
	
	private static final String SITE_NAME = "The Movie Database (TMDB)";
	
	@Value("${themoviedb.apikey:#{null}}")
	private String apiKey;
	
	private TmdbApi tmdbApi;
	
	private static final Pattern imdbUrl = Pattern.compile("^(http|https):\\/\\/?(www\\.)?imdb.com\\/title\\/(?<externalId>.[a-zA-Z0-9]*)(\\/*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern tmdbUrl = Pattern.compile("^(http|https):\\/\\/?(www\\.)?themoviedb.org\\/movie\\/(?<id>.[0-9]*)(.*)", Pattern.CASE_INSENSITIVE);
	
	public ThemoviedbApi() {
		super();
	}

	public ThemoviedbApi(String apiKey) {
		super();
		this.apiKey = apiKey;
	}

	public TmdbApi getApi() {
		if (this.tmdbApi==null) {
			this.tmdbApi = new TmdbApi(apiKey);
		}
		return this.tmdbApi;
	}
	
	@Override
	public ExtractedMetadata lookupContent(String sourceId) {
		
		//TMDB
		{
			Matcher matcher = tmdbUrl.matcher(sourceId);	
			if (matcher.matches()) {
				int id = Integer.parseInt(matcher.group("id"));
				return parse(find(id), sourceId);
			}
		}
		
		
		//IMDb
		{
			Matcher matcher = imdbUrl.matcher(sourceId);
			if (matcher.matches()) {
				String externalId = matcher.group("externalId");
				return findImdb(externalId)
						.<ExtractedMetadata>map(movieDb->parse(movieDb, sourceId))
						.orElse(new ExtractedMetadata());
			}
		}
		
				
		return null;
	}
	
	private ExtractedMetadata parse(MovieDb movie, String sourceId) {
		ExtractedMetadata exurl = new ExtractedMetadata();
		exurl.setSiteName(SITE_NAME);
		exurl.setContentType(MediaType.TEXT_HTML_VALUE);
		if (sourceId.contains("themoviedb.org")) {
			exurl.setUrl(sourceId);
		} else {
			exurl.setUrl(
					UriComponentsBuilder
						.fromHttpUrl(URL)
						.build(Map.of("id", movie.getId()))
						.toString());
		}
		LocalDate localDate = LocalDate.parse(movie.getReleaseDate());
		exurl.setTitle(movie.getTitle()+ " ("+localDate.getYear()+") - TMDB");
		exurl.setDescription(movie.getOverview());
		exurl.setImageUrl(
				UriComponentsBuilder
					.fromHttpUrl(IMAGE_URL_PREFIX)
					.path(movie.getPosterPath())
					.build()
					.toString());
		return exurl;
	}
		
	private Optional<MovieDb> findImdb(String externalId) {
		TmdbFind find = getApi().getFind();
		FindResults results = find.find(externalId, ExternalSource.imdb_id, "en-US");
		return results.getMovieResults().stream().findFirst();
	}
	
	private MovieDb find(int movieId) {
		TmdbMovies movies = getApi().getMovies();
		return movies.getMovie(movieId, "en-US", MovieMethod.release_dates);
	}

}
