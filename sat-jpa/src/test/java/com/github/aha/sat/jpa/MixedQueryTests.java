package com.github.aha.sat.jpa;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.aha.sat.jpa.domain.City;

class MixedQueryTests extends AbstractCityTests {

    @Test
	void testCitiesWithState() {
		List<City> result = cityRepository.findByState("CA");
		assertThat(result.size(), equalTo(1));
        City sf = result.get(0);
		assertThat(sf.getName(), equalTo("San Francisco"));
		assertThat(sf.getCountry(), equalTo("USA"));
    }

}
