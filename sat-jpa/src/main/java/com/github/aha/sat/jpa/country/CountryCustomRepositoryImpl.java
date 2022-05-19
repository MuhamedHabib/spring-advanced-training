package com.github.aha.sat.jpa.country;

import static com.github.aha.sat.jpa.city.QCity.city;
import static com.github.aha.sat.jpa.country.QCountry.country;
import static java.util.Objects.nonNull;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import com.github.aha.sat.jpa.city.CityProjection;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CountryCustomRepositoryImpl implements CountryCustomRepository {

	@PersistenceContext
	private final EntityManager em;

	public List<Country> findAllCountriesBy(@NonNull String cityName, @NonNull String cityState) {
		return new JPAQuery<Country>(em)
				.select(city.country)
				.from(city)
				.where(city.name.like(cityName)
						.and(city.state.like(cityState)))
				.fetch();
	}

	public List<CityProjection> searchByCountry(@NonNull String countryName) {
		return new JPAQuery<CityProjection>(em)
				.select(Projections.constructor(CityProjection.class, city.id, city.name, city.state, city.country.name))
				.from(city)
				.where(city.country.name.eq(countryName))
				.fetch();
	}

	public long countBy(String cityName, String cityState, String countryName) {
		JPAQuery<Long> query = new JPAQuery<>(em)
				.select(country.count())
				.from(country)
				.innerJoin(country.cities, city);
		if (nonNull(countryName)) {
			query.where(country.name.eq(countryName));
		}
		if (nonNull(cityName)) {
			query.where(city.name.contains(cityName));
		}
		if (nonNull(cityState)) {
			query.where(city.state.like(cityState));
		}
		return query.fetchOne();
	}

	public List<Tuple> countCitiesInCountriesLike(@NonNull String countryName) {
		return new JPAQuery<>(em)
				.select(city.country.id.as("countryId"), city.country.name, city.country.count())
				.from(city)
				.where(city.country.name.like(countryName))
				.groupBy(city.country)
				.fetch();
	}

}