package com.xperia64.storagetest;

import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    Button permissionBtn;
    Button writeBtn;

    Uri folderToWriteTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // The button that will prompt the user for a directory to write the file in
        permissionBtn = (Button) findViewById(R.id.reqPermBtn);
        permissionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestExternalStorageAccess();
            }
        });

        // The button that will actually write the file
        writeBtn = (Button) findViewById(R.id.writeFileBtn);
        writeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createFile();
            }
        });
    }

    // Arbitrary ID for the external storage permission request
    final int REQ_PERM_ID = 23;
    public void requestExternalStorageAccess()
    {
        // Release the existing permissions if there are any
        List<UriPermission> permissions = getContentResolver().getPersistedUriPermissions();
        if (!permissions.isEmpty()) {
            for (UriPermission p : permissions) {
                getContentResolver().releasePersistableUriPermission(p.getUri(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        }
        // Ask the user for a folder
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQ_PERM_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQ_PERM_ID) {
            if (resultCode == RESULT_OK) {

                // Get te Uri to access the selected folder and take its permissions
                Uri treeUri = data.getData();
                getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                folderToWriteTo = treeUri;
            } else {
                Toast.makeText(this, "Could not take permission", Toast.LENGTH_LONG).show();
            }

        }
    }


    final String myMessage = "Hello World!\n";

    // This writes a file via a DocumentFile's OutputStream
    public void createFile()
    {
        if(folderToWriteTo!=null)
        {
            // Create a DocumentFile from our Uri
            DocumentFile root = DocumentFile.fromTreeUri(this, folderToWriteTo);

            // Create a new file in our root directory
            DocumentFile myNewFile = root.createFile("application/octet-stream", "myNewFile.txt");
            try {
                // Write our message
                DataOutputStream os = new DataOutputStream(new BufferedOutputStream(getContentResolver().openOutputStream(myNewFile.getUri())));
                os.write(myMessage.getBytes());
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(myNewFile.length()>0)
            {
                Toast.makeText(this, "Wrote file successfully", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(this, "Failed to write file (size is 0)", Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(this, "No folder to write to!", Toast.LENGTH_LONG).show();
        }
    }



}
