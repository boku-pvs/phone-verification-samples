General Guidelines About The iOS Sample Code:

1. In Info.plist, the file has 'auth.svcs.verizon.com' as one of its NSExceptionDomains. This is needed, because Verizon redirects the Phone Verification API call to a HTTPS URL. In order for the redirect to work, Verizon's domain name has to be indicated in Info.plist.

2. InitialViewController.swift is the view controller for the landing page of the app. 

3. VerifySMSCodeViewController.swift is the view controller for the SMS verification page.

4. SuccessViewController is the view controller for the page indicating a successful phone verification process. 

5. FailViewController is the view controller for the page indicating a failed phone verification process. 