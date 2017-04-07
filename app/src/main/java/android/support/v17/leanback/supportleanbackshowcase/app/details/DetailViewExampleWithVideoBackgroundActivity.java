package android.support.v17.leanback.supportleanbackshowcase.app.details;

import android.os.Bundle;
import android.support.v17.leanback.app.DetailsFragment;

/**
 * Contains a {@link DetailsFragment} in order to display more details for a given card.
 */

public class DetailViewExampleWithVideoBackgroundActivity extends DetailViewExampleActivity {

    /***
     * True if using trailer as a background.
     */
    protected boolean hasBackgroundVideo() {
        return true;
    }
}
