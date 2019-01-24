package com.boku.samples.phoneverification.servlet;

import com.boku.samples.phoneverification.Authorization;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthorizationServlet extends HttpServlet {

    //TODO: configure these values received during API signup
    private static final String AES_KEY = "AES_KEY";
    private static final String DEVELOPER_ID = "DEVELOPER_ID";
    private static final String ENCRYPTION_TYPE = "AES/CTR/NoPadding"; //default, check it is correct
    private static final String BOKU_BASE_URL = "https://BOKU_BASE_URL/api/v1/";
    
	private static final Gson GSON = new Gson();

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response, Map<String, String> body)
            throws ServletException, IOException {
        System.out.println("Received request: " + request.getMethod() + ", " + request.getRequestURL() + ", " + body);
        
        String mobileNumber = request.getParameter("mobileNumber");
        if (mobileNumber == null && body != null) {
            //check the post body
            mobileNumber = body.get("mobileNumber");
        }

        if (mobileNumber != null && mobileNumber.trim().length() == 12) {
            try {
                Authorization authorization = Authorization.builder()
                        .aesKey(AES_KEY)
                        .developerId(DEVELOPER_ID)
                        .encryptionType(ENCRYPTION_TYPE)
                        .build();
                String auth = authorization.generate();

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("verifyPhoneNumber", getEncodedApiUrl("verifyPhoneNumber/", mobileNumber, auth));
                responseMap.put("verifySMSCode", getEncodedApiUrl("verifySMSCode/", mobileNumber, auth));
                responseMap.put("resendCode", getEncodedApiUrl("resendCode/", mobileNumber, auth));
                String responseJson = GSON.toJson(responseMap);

                OutputStream out = response.getOutputStream();
                out.write(responseJson.getBytes());
                out.flush();
            } catch (Exception ex) {
                System.err.println("AuthorizationServlet: ERROR in processRequest ex=" + ex.getMessage());
                ex.printStackTrace();
            }
        } else {
            //error
            System.err.println("AuthorizationServlet: ERROR in processRequest, invalid mobileNumber=" + mobileNumber);
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response, null);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        Map<String, String> body = GSON.fromJson(requestBody, Map.class);
        processRequest(request, response, body);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }

    private Object getEncodedApiUrl(String context, String mobileNumber, String auth) throws UnsupportedEncodingException {
        return URLEncoder.encode(BOKU_BASE_URL + context + URLEncoder.encode(mobileNumber, "UTF-8") + "/?authToken=" + URLEncoder.encode(auth, "UTF-8"), "UTF-8");
    }
}
