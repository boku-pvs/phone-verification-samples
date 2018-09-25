import UIKit

class FailViewController: UIViewController {
    
    @IBOutlet weak var FailMessage: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let appDelegate = UIApplication.shared.delegate as! AppDelegate
        FailMessage.text = appDelegate.phoneNumber! + " does not belong to this device"
    }
    
    override func viewWillAppear(_ animated: Bool) {
        self.navigationItem.setHidesBackButton(true, animated:true)
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
    
    @IBAction func startOver(_ sender: Any) {
        DispatchQueue.main.async(execute: {
            let _ = self.navigationController?.popToRootViewController(animated: true)
        })
    }
    
}
