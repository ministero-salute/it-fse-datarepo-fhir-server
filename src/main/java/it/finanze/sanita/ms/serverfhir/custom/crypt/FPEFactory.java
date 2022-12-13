package it.finanze.sanita.ms.serverfhir.custom.crypt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.voltage.securedata.enterprise.FPE;
import com.voltage.securedata.enterprise.AES;
import com.voltage.securedata.enterprise.LibraryContext;

@Component
public class FPEFactory {

	private static final String AES = "AES";

	@Value("${crypt.securedata.policyURL}")
	private String policyURL;

	@Value("${crypt.securedata.trustStorePath")
    private String trustStorePath;

	@Value("${crypt.securedata.identity")
    private String identity;
    
	@Value("${crypt.securedata.username")
    private String username;
    
	@Value("${crypt.securedata.password")
    private String password;
    
	@Value("${crypt.securedata.format")
    private List<String> formats;

	private AES aes = null;
	private Map<String, FPE> fpedictionary = new HashMap<String, FPE>();
    private LibraryContext library = null;
    private Exception startupex = null;
    
    @PostConstruct
	void postConstruct() {               
    	createSharedLibraryContext();
    }
    
	void createSharedLibraryContext() {               
        try {

            // Create the LibraryContext instance that will be shared by all
            // of the FPE instances created by the factory.
        	              
            library = new LibraryContext.Builder()
                            .setPolicyURL(policyURL)
                            .enableMemoryCache(true)
                            .setTrustStorePath(trustStorePath)
                            .setClientIdProduct("Simple_API_Java_FactorySample", "1.8")
                            .build();
            
            for (String format : formats) {
            	if (FPEFactory.AES.equals(format)) {
            		aes = library.getAESBuilder()
                            .setUsernamePassword(username,password)
                            .setIdentity(identity)
                            .build();
            	} else {
	            	FPE.Builder key = library.getFPEBuilder(format)
	                        .setUsernamePassword(username,password)
	                        .setIdentity(identity);
	                
	                //library.keyWarmerAddFPE(key);
	                
	                FPE fpe = key.build();
	                fpedictionary.put(format, fpe);
            	}
			}

        } catch (Exception ex) {
        	startupex = ex;
        } 

	}

	//TODO: CALL ME ON TURN OFF
	void deleteSharedLibraryContext() {               
        // Now it is safe to delete the shared LibraryContext instance.
        for (FPE value : fpedictionary.values()) {
            value.delete();
        }
        if (library != null) {
            library.delete();

        }
    }
	
	 // This method either returns an FPE object from the HashMap dictionary
    // when the FPE.Builder instance, used as the dictionary key, finds a
    // match, or it builds a new FPE instance for this combination of
    // parameters, adds it to the dictionary, and then returns it.
    public FPE getFPE(String format) throws FactoryException {
    	FPE fpe = fpedictionary.get(format);
    	if(fpe == null && startupex != null) {
    		throw new FactoryException("errore di caricamento FPE", startupex);
    	}
        return fpe;
    }
    
	public AES getAES() throws FactoryException {
    	if(aes == null) {
    		throw new FactoryException("AES non presente in configurazione");
    	} else if( startupex != null) {
    		throw new FactoryException("errore di caricamento AES", startupex);
    	}
        return aes;
    }
	
}
