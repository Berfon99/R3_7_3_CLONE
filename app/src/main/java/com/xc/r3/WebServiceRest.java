package com.xc.r3;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import timber.log.Timber;


public class WebServiceRest {
	private static final String URL_DOWNLOAD = "https://www.googleapis.com/drive/v3/files/";
    private static final String URL_LISTER =
			"https://www.googleapis.com/drive/v3/files?orderBy=modifiedTime%20desc&q=name%20contains%20'OA'%20and%20mimeType%20%3D'text%2Fplain'%20and%20trashed%20=%20false";
    public static final String ERREUR = "erreur";
	private final String token;

    public WebServiceRest(String token) {
		this.token=token;
	}

	public String downloader(Fichier fichier) {
        String reponse;
        String url = URL_DOWNLOAD + fichier.getId() + "?alt=media";
        reponse = executerRequete(url,false);
        return reponse;
	}

	public String getListeFichiersGson() {
		String reponse;
		reponse = executerRequete(URL_LISTER,true);
		return reponse;
	}

	private String executerRequete(String url, boolean json) {
		String reponse = ERREUR;
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader("Authorization" , "Bearer " + this.token);
			if (json) {
				httpGet.addHeader("Accept", "application/json");
			}
			HttpResponse response = httpClient.execute(httpGet);
			String phrase = response.getStatusLine().getReasonPhrase();
			int codeRetour=response.getStatusLine().getStatusCode();
			Timber.i( "Token : %s", token);
			Timber.i( "Reponse : %s", phrase);
			Timber.i( "Status : %s", codeRetour);
			if ( codeRetour== HttpStatus.SC_OK) {
				HttpEntity httpEntity = response.getEntity();
				reponse = EntityUtils.toString(httpEntity, HTTP.UTF_8);
			} else {
				reponse = "" + codeRetour;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return reponse;
	}
}
