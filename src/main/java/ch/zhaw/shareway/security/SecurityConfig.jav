@Bean 
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception { 
http 
.authorizeHttpRequests(authorize -> authorize 
.requestMatchers("/*").permitAll() 
.requestMatchers("/api/**").authenticated() 
.requestMatchers("/**").permitAll()            
) 
.oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults())); 
return http.build(); 
} 