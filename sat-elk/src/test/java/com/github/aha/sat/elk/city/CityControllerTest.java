package com.github.aha.sat.elk.city;

import static com.github.aha.sat.elk.city.City.INDEX;
import static com.github.aha.sat.elk.city.CityController.ROOT_PATH;
import static java.lang.Float.NaN;
import static java.lang.Long.valueOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.data.elasticsearch.core.TotalHitsRelation.EQUAL_TO;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHitsImpl;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CityController.class)
class CityControllerTest {

	private static final String CITY_ID = UUID.randomUUID().toString();
	private static final String CITY_NAME = "Barcelona";
	private static final String CITY_COUNTRY = "Spain";
	private static final String CITY_SUBCOUNTRY = "Catalunya";
	private static final Integer CITY_GEONAMEID = 3128760;

	@MockBean
	private CityService service;

	@Autowired
	private MockMvc mvc;

	@Test
	void getCityById() throws Exception {
		given(service.findById(CITY_ID)).willReturn(new City(CITY_ID, CITY_NAME, CITY_COUNTRY, CITY_SUBCOUNTRY, valueOf(CITY_GEONAMEID)));

		mvc.perform(get(ROOT_PATH + "/" + CITY_ID))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
				.andExpect(jsonPath("$.id", is(CITY_ID)))
				.andExpect(jsonPath("$.name", is(CITY_NAME)))
				.andExpect(jsonPath("$.country", is(CITY_COUNTRY)))
				.andExpect(jsonPath("$.subcountry", is(CITY_SUBCOUNTRY)))
				.andExpect(jsonPath("$.geonameid", is(CITY_GEONAMEID)));
	}

	@Test
	void searchByCountry() throws Exception {
		var secondCity = "Madrid";
		List<City> cities = List.of(
				new City(CITY_ID, CITY_NAME, CITY_COUNTRY, CITY_SUBCOUNTRY, CITY_GEONAMEID.longValue()),
				new City(UUID.randomUUID().toString(), secondCity, CITY_COUNTRY, secondCity, 3117735L));
		given(service.searchByCountry(eq(CITY_COUNTRY), any())).willReturn(new PageImpl<City>(cities));

		mvc.perform(get(ROOT_PATH + "/country/" + CITY_COUNTRY))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
				.andExpect(jsonPath("$.content[0].id", is(CITY_ID)))
				.andExpect(jsonPath("$.content[0].name", is(CITY_NAME)))
				.andExpect(jsonPath("$.content[1].name", is(secondCity)))
				.andExpect(jsonPath("$.totalElements", is(cities.size())))
				.andExpect(jsonPath("$.totalPages", is(1)))
				.andExpect(jsonPath("$.number", is(0)));
	}

	@Test
	void search() throws Exception {
		String cityNameParamValue = CITY_NAME.toLowerCase().substring(0, 5);
		Object[] sortedValues = List.of(cityNameParamValue).toArray();
		var cityHit = new SearchHit<City>(
				INDEX, UUID.randomUUID().toString(), null, NaN, sortedValues, null, null, null, null, null,
				new City(CITY_ID, CITY_NAME, CITY_COUNTRY, CITY_SUBCOUNTRY, CITY_GEONAMEID.longValue()));
		List<? extends SearchHit<City>> cities = List.of(cityHit);
		given(service.search(eq(cityNameParamValue), eq(CITY_COUNTRY), any(), any()))
				.willReturn(new SearchHitsImpl<City>(1, EQUAL_TO, NaN, "scrollId", cities, null, null));

		mvc.perform(get(ROOT_PATH + "?name=" + cityNameParamValue + "&country=" + CITY_COUNTRY + "&size=5&sort=name"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
				.andExpect(jsonPath("$.searchHits[0].index", is(INDEX)))
				.andExpect(jsonPath("$.searchHits[0].id", notNullValue()))
				.andExpect(jsonPath("$.searchHits[0].score", is("NaN")))
				.andExpect(jsonPath("$.searchHits[0].sortValues.[0]", is(cityNameParamValue)))
				.andExpect(jsonPath("$.searchHits[0].content.id", notNullValue()))
				.andExpect(jsonPath("$.searchHits[0].content.name", is(CITY_NAME)))
				.andExpect(jsonPath("$.searchHits[0].content.country", is(CITY_COUNTRY)))
				.andExpect(jsonPath("$.searchHits[0].content.subcountry", is(CITY_SUBCOUNTRY)))
				.andExpect(jsonPath("$.searchHits[0].content.geonameid", is(CITY_GEONAMEID)))
				.andExpect(jsonPath("$.totalHits", is(1)));
	}

	@Test
	void uploadFile() throws Exception {
		mvc.perform(post(ROOT_PATH + "/upload"))
				.andExpect(status().isNoContent());
	}

}
