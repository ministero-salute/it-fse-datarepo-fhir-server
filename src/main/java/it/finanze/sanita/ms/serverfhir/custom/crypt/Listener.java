package it.finanze.sanita.ms.serverfhir.custom.crypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

import javax.persistence.PostLoad;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.jpa.model.entity.ResourceHistoryTable;

@Component
public class Listener {
	

	@Value("${crypt.status}")
	private Boolean cryptStatus;
	
	@PostLoad
	@PostUpdate
	//@PostPersist
	public void decrypt(Object pc) {
		if(cryptStatus) {
			if (!(pc instanceof ResourceHistoryTable)) {
				return;
			}
			ResourceHistoryTable rt = (ResourceHistoryTable) pc;
			byte[] cryptedContent = rt.getResource();
			byte[] decryptedContent = decrypt(cryptedContent);
			rt.setResource(decryptedContent);
//		try {
//			System.out.println("TESTO DECRIPTATO: " + new String(decompressGzip(rt.getResource())));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		}
	}

   @PrePersist
   @PreUpdate
   public void encrypt(Object pc) {
	   if(cryptStatus) {
		   if (!(pc instanceof ResourceHistoryTable)) {
			   return;
		   }
		   
		   ResourceHistoryTable rt = (ResourceHistoryTable) pc;
		   try {
			   System.out.println("TESTO CHE NON SIA CRIPTATO " + new String(decompressGzip(rt.getResource())));
			   System.out.println("Lo crypto prima di inserire su db");
			   rt.setResource(crypt(rt.getResource()));
		   } catch (IOException e) {
			   // TODO Auto-generated catch block
			   e.printStackTrace();
		   }
	   }
   }
   
   
   private byte[] crypt(byte[] word) {
	   return Base64.getEncoder().encode(word);
	}
		   
   private byte[] decrypt(byte[] word) {
	   return Base64.getDecoder().decode(word);
	}

    private static byte[] decompressGzip(byte[] source) throws IOException {

    	GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(source));
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) > 0) {
            	baos.write(buffer);
            }

        return baos.toByteArray();
    }
}