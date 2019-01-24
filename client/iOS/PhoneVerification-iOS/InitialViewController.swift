import UIKit

class InitialViewController: UIViewController {
    
    @IBOutlet weak var PhoneNumberTextField: UITextField!
    @IBOutlet weak var verifyPhoneButton: UIButton!
    
    var verifySMSCodeURL:String?
    var verifyPhoneNumberURL:String?
    var resendCodeURL:String?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        PhoneNumberTextField.keyboardType = UIKeyboardType.numberPad
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
    
    @IBAction func verifyPhoneNumber(_ sender: Any) {
        if(PhoneNumberTextField.text?.count != 10) {
            createAlert(title: "Invalid Phone Number", message: "Please enter a valid 10-digit number")
            return
        }
        
        self.enableButtonsAndTextFields(enable: false)
        _ = self.spinner(self.view, startAnimate: true)
        
        // Using a semaphore to make two synchronous URL requests. 
        // The first request returns the API endpoints. And the second request calls the Phone Verification API. 
        let semaphore = DispatchSemaphore(value: 0)
        
        // The following URL request is for the developer's server.
        // It returns the three necessary endpoints for phone verification, which are:
        // 1. https://api.phoneverification.io/verifyPhoneNumber/phoneNumber
        // 2. https://api.phoneverification.io/verifySMSCode/phoneNumber
        // 3. https://api.phoneverification.io/resendCode/phoneNumber
        var url = URL(string: "SERVER URL" + PhoneNumberTextField.text!)!
        var request = URLRequest(url: url)
        request.httpMethod = "GET"

        var task = URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data, error == nil else { // check for fundamental networking error
                DispatchQueue.main.async {
                    self.createAlert(title: "Error!", message: "\(String(describing: error))" )
                    self.enableButtonsAndTextFields(enable: true)
                }
                
                return
            }

            if let httpStatus = response as? HTTPURLResponse, httpStatus.statusCode != 200 { // check for http errors
                DispatchQueue.main.async {
                    self.createAlert(title: "Error!", message: "HTTP status code should be 200, but is \(httpStatus.statusCode)\nResponse received: \(String(describing: response))")
                    self.enableButtonsAndTextFields(enable: true)
                }
                
                return
            }

            do {
                let json = try JSONSerialization.jsonObject(with: data, options: .allowFragments) as! [String:Any]
    
                let appDelegate = UIApplication.shared.delegate as! AppDelegate
                    
                appDelegate.phoneNumber = self.PhoneNumberTextField.text
                
                // Storing the three necessary endpoints
                appDelegate.verifySMSCodeURL = json["verifySMSCode"] as? String
                appDelegate.verifySMSCodeURL = appDelegate.verifySMSCodeURL?.removingPercentEncoding
                    
                appDelegate.verifyPhoneNumberURL = json["verifyPhoneNumber"] as? String
                appDelegate.verifyPhoneNumberURL = appDelegate.verifyPhoneNumberURL?.removingPercentEncoding
                    
                appDelegate.resendCodeURL = json["resendCode"] as? String
                appDelegate.resendCodeURL = appDelegate.resendCodeURL?.removingPercentEncoding
            } catch let error as NSError {
                DispatchQueue.main.async {
                    self.createAlert(title: "Error!", message: "\(String(describing: error))" )
                    self.enableButtonsAndTextFields(enable: true)
                }
            }
            
            semaphore.signal()
        }

        task.resume()
        semaphore.wait()
        
        // The following URL request is an API call to Boku's phone verification process.
        let appDelegate = UIApplication.shared.delegate as! AppDelegate
        url = URL(string: appDelegate.verifyPhoneNumberURL!)!
        request = URLRequest(url: url)
        request.httpMethod = "POST"
            
        task = URLSession.shared.dataTask(with: request) { data, response, error in
            self.enableButtonsAndTextFields(enable: true)
            _ = self.spinner(self.view, startAnimate: false)
            
            guard let data = data, error == nil else { // check for fundamental networking error
                DispatchQueue.main.async {
                    self.createAlert(title: "Error!", message: "\(String(describing: error))" )
                }
        
                return
            }
            
            if let httpStatus = response as? HTTPURLResponse, httpStatus.statusCode != 200 { // check for http errors
                DispatchQueue.main.async {
                    self.createAlert(title: "Error!", message: "HTTP status code should be 200, but is \(httpStatus.statusCode)\nResponse received: \(String(describing: response))")
                }
                
                return
            }
            
            do {
                let json = try JSONSerialization.jsonObject(with: data, options: .allowFragments) as! [String:Any]
                
                let statusCode = json["status"] as! Int
            
                if(statusCode == PHONE_VERIFICATION_SUCCESS) {
                    DispatchQueue.main.async {
                        self.performSegue(withIdentifier: "toSuccess", sender: self)
                    }
                } else if(statusCode == PHONE_VERIFICATION_FAILED) {
                    DispatchQueue.main.async {
                        self.performSegue(withIdentifier: "toFailure", sender: self)
                    }
                } else if(statusCode == PHONE_VERIFICATION_SMS) {
                    DispatchQueue.main.async {
                        self.performSegue(withIdentifier: "toSmsCode", sender: self)
                    }
                } else if(statusCode == PHONE_VERIFICATION_ERROR) {
                    DispatchQueue.main.async {
                        self.createAlert(title: "Phone Verification Failed", message: json["statusMessage"] as! String)
                    }
                }
            } catch let error as NSError {
                DispatchQueue.main.async {
                    self.createAlert(title: "Error!", message: "\(String(describing: error))" )
                    self.enableButtonsAndTextFields(enable: true)
                }
            }

            semaphore.signal()
        }
        
        task.resume()
        semaphore.wait()
    }

    // This function creates a popup for whenever an error ocurred. 
    func createAlert(title:String, message:String) {
        let alert = UIAlertController(title: title, message: message, preferredStyle: UIAlertControllerStyle.alert)
        
        alert.addAction(UIAlertAction(title: "OK", style: UIAlertActionStyle.default, handler: { (action) in
            
        }))
        
        self.present(alert, animated: true, completion: nil)
    }
    
    func spinner (_ viewContainer: UIView, startAnimate:Bool? = true) -> UIActivityIndicatorView {
        let mainContainer: UIView = UIView(frame: viewContainer.frame)
        mainContainer.center = viewContainer.center
        mainContainer.backgroundColor = UIColor(white: 1, alpha: 0.5)
        mainContainer.alpha = 0.5
        mainContainer.tag = 888
        mainContainer.isUserInteractionEnabled = false
        
        let viewBackgroundLoading: UIView = UIView(frame: CGRect(x:0,y: 0,width: 80,height: 80))
        viewBackgroundLoading.center = viewContainer.center
        viewBackgroundLoading.backgroundColor = UIColor.darkGray
        viewBackgroundLoading.alpha = 0.5
        viewBackgroundLoading.clipsToBounds = true
        viewBackgroundLoading.layer.cornerRadius = 15
        
        let activityIndicatorView: UIActivityIndicatorView = UIActivityIndicatorView()
        activityIndicatorView.frame = CGRect(x:0.0,y: 0.0,width: 40.0, height: 40.0)
        activityIndicatorView.activityIndicatorViewStyle =
            UIActivityIndicatorViewStyle.whiteLarge
        activityIndicatorView.center = CGPoint(x: viewBackgroundLoading.frame.size.width / 2, y: viewBackgroundLoading.frame.size.height / 2)
        if startAnimate!{
            viewBackgroundLoading.addSubview(activityIndicatorView)
            mainContainer.addSubview(viewBackgroundLoading)
            viewContainer.addSubview(mainContainer)
            activityIndicatorView.startAnimating()
        }else{
            for subview in viewContainer.subviews{
                if subview.tag == 888 {
                    subview.removeFromSuperview()
                }
            }
        }
        return activityIndicatorView
    }
    
    func enableButtonsAndTextFields(enable: Bool) {
        PhoneNumberTextField.isEnabled = enable
        verifyPhoneButton.isEnabled = enable
    }
    
}
