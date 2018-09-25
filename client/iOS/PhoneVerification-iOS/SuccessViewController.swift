import UIKit

class SuccessViewController: UIViewController {

    @IBOutlet weak var SuccessMessage: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let appDelegate = UIApplication.shared.delegate as! AppDelegate
        SuccessMessage.text = appDelegate.phoneNumber! + " belongs to this device"
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
