import UIKit

class VerifySMSCodeViewController: UIViewController {
    
    @IBOutlet weak var smsCodeTextField: UITextField!
    @IBOutlet weak var verifySMSButton: UIButton!
    @IBOutlet weak var resendCodeButton: UIButton!
    @IBOutlet weak var text: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
    }
    
    @IBAction func verifySMS(_ sender: Any) {
        if(smsCodeTextField.text?.count == 0) {
            createAlert(title: "Invalid Code", message: "Please enter the SMS code")
            return
        }
        
        enableButtonsAndTextFields(enable: false)
        _ = spinner(self.view, startAnimate: true)
        
        let appDelegate = UIApplication.shared.delegate as! AppDelegate
        let url = URL(string: appDelegate.verifySMSCodeURL! + "&code=" + smsCodeTextField.text!)!
        
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data, error == nil else {
                // check for fundamental networking error
                DispatchQueue.main.async {
                    self.createAlert(title: "Error!", message: "\(String(describing: error))")
                    
                    self.enableButtonsAndTextFields(enable: true)
                }
                
                return
            }
            
            if let httpStatus = response as? HTTPURLResponse, httpStatus.statusCode != 200 {
                // check for http errors
                DispatchQueue.main.async {
                    self.createAlert(title: "Error!", message: "HTTP status code should be 200, but is \(httpStatus.statusCode)\nResponse is: \(String(describing: response))")
                    
                    self.enableButtonsAndTextFields(enable: true)
                }
                
                return
            }
            
            do {
                self.smsCodeTextField.isEnabled = true
                self.verifySMSButton.isEnabled = true
                self.resendCodeButton.isEnabled = true
                
                let json = try JSONSerialization.jsonObject(with: data, options: .allowFragments) as! [String:Any]
                
                let statusCode = json["status"] as! Int
                
                _ = self.spinner(self.view, startAnimate: false)
                
                if(statusCode == PHONE_VERIFICATION_SUCCESS) {
                    DispatchQueue.main.async {
                        self.performSegue(withIdentifier: "smsToSuccess", sender: self)
                    }
                } else if(statusCode == PHONE_VERIFICATION_FAILED) {
                    DispatchQueue.main.async {
                        self.performSegue(withIdentifier: "smsToFailure", sender: self)
                    }
                } else if(statusCode == PHONE_VERIFICATION_ERROR) {
                    DispatchQueue.main.async {
                        self.createAlert(title: "Phone Verification Failed", message: json["statusMessage"] as! String)
                        self.smsCodeTextField.text = ""
                    }
                }
            } catch let error as NSError {
                DispatchQueue.main.async {
                    self.createAlert(title: "Error!", message: "\(String(describing: error))" )
                    
                    self.enableButtonsAndTextFields(enable: true)
                }
            }
        }
        
        task.resume()
    }
    
    @IBAction func resendCode(_ sender: Any) {
        
        enableButtonsAndTextFields(enable: false)
        _ = spinner(self.view, startAnimate: true)
        
        let appDelegate = UIApplication.shared.delegate as! AppDelegate
        let url = URL(string: appDelegate.resendCodeURL!)!
        
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            
            self.enableButtonsAndTextFields(enable: true)
            
            guard let _ = data, error == nil else { // check for fundamental networking error
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
                _ = self.spinner(self.view, startAnimate: false)
                
                let json = try JSONSerialization.jsonObject(with: data!, options: .allowFragments) as! [String:Any]
                
                let statusCode = json["status"] as! Int
                
                if(statusCode == -1) {
                    DispatchQueue.main.async {
                        self.createAlert(title: "Resend Code Failed", message: json["statusMessage"] as! String)
                    }
                } else if(statusCode == 1) {
                    DispatchQueue.main.async {
                        self.createAlert(title: "Resend Code Success", message: json["statusMessage"] as! String)
                    }
                }
            } catch let error as NSError {
                DispatchQueue.main.async {
                    self.createAlert(title: "Error!", message: "\(String(describing: error))" )
                    
                    self.enableButtonsAndTextFields(enable: true)
                }
            }
        }
        
        task.resume()
    }
    
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
        self.smsCodeTextField.isEnabled = enable
        self.verifySMSButton.isEnabled = enable
        self.resendCodeButton.isEnabled = enable
    }
}
