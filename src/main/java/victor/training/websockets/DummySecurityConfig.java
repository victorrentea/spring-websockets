//package victor.training.websockets;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.provisioning.InMemoryUserDetailsManager;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
//public class DummySecurityConfig extends WebSecurityConfigurerAdapter {
//   @Override
//   protected void configure(HttpSecurity http) throws Exception {
//       http
//               .csrf().disable()
//               .authorizeRequests()
//               .anyRequest().authenticated()
//               .and()
//               .formLogin().permitAll()
//               .and().httpBasic()
//       ;
//   }
//
//   // *** Dummy users 100% in-mem - NEVER USE IN PRODUCTION
//   @Bean
//   public UserDetailsService userDetailsService() {
//      UserDetails userDetails = User.withDefaultPasswordEncoder()
//          .username("user").password("user").roles("USER").build();
//      UserDetails adminDetails = User.withDefaultPasswordEncoder()
//          .username("admin").password("admin").roles("ADMIN").build();
//      return new InMemoryUserDetailsManager(userDetails, adminDetails);
//   }
//
//   @GetMapping("current-user")
//   public String user() {
//      return SecurityContextHolder.getContext().getAuthentication().getName();
//   }
//
//}
