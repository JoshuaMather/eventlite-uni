package uk.ac.man.cs.eventlite.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
public class Security extends WebSecurityConfigurerAdapter {

	public static final String ADMIN_ROLE = "ADMINISTRATOR";
	public static final String EVENT_ORGANISER = "ORGANISER";

	// List the mappings/methods for which no authorisation is required.
	// By default we allow all GETs and full access to the H2 console.
	private static final RequestMatcher[] NO_AUTH = { new AntPathRequestMatcher("/webjars/**", "GET"),
			new AntPathRequestMatcher("/h2-console/**"), new AntPathRequestMatcher("/events/{id:[\\d]+}", "GET"), 
			new AntPathRequestMatcher("/events", "GET"), new AntPathRequestMatcher("/events/show", "GET"),
			new AntPathRequestMatcher("/events/search/*", "GET"), new AntPathRequestMatcher("/api/**", "GET")  ,
			new AntPathRequestMatcher("/venues/{id:[\\d]+}", "GET"), 
			new AntPathRequestMatcher("/venues", "GET"), new AntPathRequestMatcher("/venues/show", "GET"),
			new AntPathRequestMatcher("/venues/search/*", "GET")};


	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// By default, all requests are authenticated except our specific list.
		http.authorizeRequests().requestMatchers(NO_AUTH).permitAll().anyRequest().hasAnyRole(ADMIN_ROLE, EVENT_ORGANISER);

		// Use form login/logout for the Web.
		http.formLogin().loginPage("/sign-in").permitAll();
		http.logout().logoutUrl("/sign-out").logoutSuccessUrl("/").permitAll();

		// Use HTTP basic for the API.
		http.requestMatcher(new AntPathRequestMatcher("/api/**")).httpBasic();

		// Only use CSRF for Web requests.
		// Disable CSRF for the API and H2 console.
		http.antMatcher("/**").csrf().ignoringAntMatchers("/api/**", "/h2-console/**");

		// Disable X-Frame-Options for the H2 console.
		http.headers().frameOptions().disable();
	}

	@Override
	@Bean
	public UserDetailsService userDetailsService() {
		PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

		UserDetails rob = User.withUsername("Rob").password(encoder.encode("Haines")).roles(ADMIN_ROLE).build();
		UserDetails caroline = User.withUsername("Caroline").password(encoder.encode("Jay")).roles(ADMIN_ROLE).build();
		UserDetails markel = User.withUsername("Markel").password(encoder.encode("Vigo")).roles(ADMIN_ROLE).build();
		UserDetails mustafa = User.withUsername("Mustafa").password(encoder.encode("Mustafa")).roles(ADMIN_ROLE)
				.build();

		UserDetails test = User.withUsername("test").password(encoder.encode("test")).roles(EVENT_ORGANISER).build();
		return new InMemoryUserDetailsManager(rob, caroline, markel, mustafa, test);
	}
}
