package de.thorstenberger.examServer.ws.remoteusermanager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationServiceException;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.providers.AuthenticationProvider;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.providers.dao.AbstractUserDetailsAuthenticationProvider;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.util.Assert;

import de.thorstenberger.examServer.model.User;
import de.thorstenberger.examServer.service.ConfigManager;
import de.thorstenberger.examServer.service.RoleManager;
import de.thorstenberger.examServer.service.UserExistsException;
import de.thorstenberger.examServer.service.UserManager;
import de.thorstenberger.examServer.util.StringUtil;
import de.thorstenberger.examServer.ws.remoteusermanager.client.UserBean;

public class HTTPAuthAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider implements AuthenticationProvider, MessageSourceAware {

	private MessageSourceAccessor messageSourceAccessor;
	private ConfigManager configManager;
	private UserManager userManager;
	private RoleManager roleManager;
	
	private boolean forcePrincipalAsString = false;
	
	/**
	 * 
	 */
	public HTTPAuthAuthenticationProvider( ConfigManager configManager, UserManager userManager, RoleManager roleManager ) {
		this.configManager = configManager;
		this.userManager = userManager;
		this.roleManager = roleManager;
	}
	
	/* (non-Javadoc)
	 * @see org.acegisecurity.providers.AuthenticationProvider#authenticate(org.acegisecurity.Authentication)
	 */
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {

		Assert.isInstanceOf(UsernamePasswordAuthenticationToken.class, authentication,
	            messageSourceAccessor.getMessage("AbstractUserDetailsAuthenticationProvider.onlySupports",
	                "Only UsernamePasswordAuthenticationToken is supported"));

		if( !configManager.isStudentsLoginEnabled() )
			throw new AuthenticationServiceException( "Login disabled for student role." );

		if( configManager.getHTTPAuthURL() != null && configManager.getHTTPAuthURL().length() > 0 ){
		
	        // Determine username
	        String username = (authentication.getPrincipal() == null) ? "NONE_PROVIDED" : authentication.getName();
	        String password = (authentication.getCredentials() == null) ? "NONE_PROVIDED" : (String)authentication.getCredentials();
			
	        UserBean userBean;
            try {
            	
            	// verify url
            	URL url = null;
            	try {
					url = new URL(configManager.getHTTPAuthURL());
				} catch (MalformedURLException e1) {
					throw new AuthenticationServiceException( "URL-Error for HTTP-Auth." );
				}
            	
				userBean = getRemoteUserInfos( url, username, password );
				
				if( !userBean.getRole().equals( "student" ) )
					throw new AuthenticationServiceException( "Only student role allowed." );
				
				
				try {
					
					userManager.getUserByUsername( userBean.getLogin() );
					
				} catch( UsernameNotFoundException e ){
					User user = new User();
					user.setEnabled( true );
					user.setUsername( userBean.getLogin() );
					user.setFirstName( userBean.getFirstName() == null ? "" : userBean.getFirstName() );
					user.setLastName( userBean.getName() == null ? "" : userBean.getName() );
					user.setEmail( userBean.getEmail() == null ? "" : userBean.getEmail() );
					user.setPassword( StringUtil.encodePassword( userBean.getPassword(), "SHA" ) );
					user.addRole( roleManager.getRole( "student" ) );
					try {
						userManager.saveUser( user );
					} catch ( UserExistsException e2 ) {
						// should not happen
						throw new RuntimeException( e2 );
					}
				}
			} catch ( AuthenticationServiceException e ) {
				throw new BadCredentialsException(messages.getMessage(
                        "AbstractUserDetailsAuthenticationProvider.badCredentials", "Invalid username or password."));
			}
			
			UserDetails userDetails = userManager.getUserByUsername( username );
	        
	        Object principalToReturn = userDetails;

	        if ( forcePrincipalAsString ) {
	            principalToReturn = userDetails.getUsername();
	        }

	        return createSuccessAuthentication( principalToReturn, authentication, userDetails );
			
		}
		
		return null;
		
	}

	private UserBean getRemoteUserInfos( URL url, String login, String pwd ) throws AuthenticationServiceException{
        try {
        	boolean auth = authenticateUser( url, login, pwd );
        	if ( auth ) {
        		String email = null;
        		if ( configManager.getHTTPAuthMail() != null && configManager.getHTTPAuthMail().length() > 0 ) {
        			email = login + '@' + configManager.getHTTPAuthMail();
        		}
        		// now we know that at least login and password are correct, so we create the user bean
        		UserBean bean = new UserBean( email, login, null, pwd, "student", null );
        		return bean;
        	}
        	throw new AuthenticationServiceException( "" );
		} catch ( IOException e ) {
			throw new AuthenticationServiceException( e.getMessage(), e );
		}
	}
	
	private boolean authenticateUser( URL url, String login, String pwd ) throws IOException {
		HttpClient client = new HttpClient();
		client.getParams().setCookiePolicy( CookiePolicy.RFC_2109 );
        client.getState().setCredentials(
            new AuthScope( url.getHost(), AuthScope.ANY_PORT, AuthScope.ANY_REALM ),
            new UsernamePasswordCredentials( login, pwd )
        );
        
        GetMethod authget = new GetMethod( url.toString() );

        authget.setDoAuthentication(true);
        
        String response = null;
        try {
            int status = client.executeMethod( authget );
            
            // check if successful
            if ( status == 200 )
            {
                response = authget.getResponseBodyAsString();

                if ( response != null ) {
                	return true;
                }
            }
        } finally {
            // release any connection resources used by the method
        	authget.releaseConnection();
        }
        
        return false;
	}
	
	/* (non-Javadoc)
	 * @see org.acegisecurity.providers.AuthenticationProvider#supports(java.lang.Class)
	 */
	public boolean supports(Class authentication) {
		return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
	}

	/* (non-Javadoc)
	 * @see org.springframework.context.MessageSourceAware#setMessageSource(org.springframework.context.MessageSource)
	 */
	public void setMessageSource(MessageSource messageSource) {
		this.messageSourceAccessor = new MessageSourceAccessor( messageSource );
	}

	/* (non-Javadoc)
	 * @see org.acegisecurity.providers.dao.AbstractUserDetailsAuthenticationProvider#additionalAuthenticationChecks(org.acegisecurity.userdetails.UserDetails, org.acegisecurity.providers.UsernamePasswordAuthenticationToken)
	 */
	@Override
	protected void additionalAuthenticationChecks(UserDetails arg0, UsernamePasswordAuthenticationToken arg1) throws AuthenticationException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.acegisecurity.providers.dao.AbstractUserDetailsAuthenticationProvider#retrieveUser(java.lang.String, org.acegisecurity.providers.UsernamePasswordAuthenticationToken)
	 */
	@Override
	protected UserDetails retrieveUser(String arg0, UsernamePasswordAuthenticationToken arg1) throws AuthenticationException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return Returns the forcePrincipalAsString.
	 */
	public boolean isForcePrincipalAsString() {
		return forcePrincipalAsString;
	}

	/**
	 * @param forcePrincipalAsString The forcePrincipalAsString to set.
	 */
	public void setForcePrincipalAsString(boolean forcePrincipalAsString) {
		this.forcePrincipalAsString = forcePrincipalAsString;
	}

}

