package gr.iti.mklab.reveal.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by kandreadou on 7/30/14.
 */
@ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR, reason="Indexing Service Exception") //500
public class RevealException extends Exception{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RevealException(String message, Throwable throwable){
        super(message, throwable);
    }
}
