package icn.icmyas.Models;

import com.parse.ParseObject;

public class IMPModel {

    private ParseObject userObject, impDetailsObject;

    public IMPModel(final ParseObject userObject, final ParseObject impDetailsObject) {
        this.userObject = userObject;
        this.impDetailsObject = impDetailsObject;
    }

    public ParseObject getUser() {
        return userObject;
    }

    public ParseObject getDetails() {
        return impDetailsObject;
    }
}
