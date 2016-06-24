package de.bitshares_munich.smartcoinswallet;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfIndirectReference;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import de.bitshares_munich.models.TransactionDetails;

/**
 * Created by developer on 5/23/16.
 */
public class pdfTable {

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private Context myContext;
    private String filename;

    public pdfTable(Context context, Activity activity, String filename) {
        verifyStoragePermissions(activity);
        this.myContext = context;
        this.filename = filename;
    }

    public static String combinePath(String path1, String path2) {
        File file1 = new File(path1);
        File file2 = new File(file1, path2);
        return file2.getPath();
    }

    private void createEmptyFile(String path)
    {
        try {
            File gpxfile = new File(path);
            FileWriter writer = new FileWriter(gpxfile);
            writer.flush();
            writer.close();
         //   Toast.makeText(myContext, myContext.getText(R.string.empty_file_created) + path, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void createTable (List<TransactionDetails> myTransactions)
    {

        Document document = new Document();

        try {
            String extStorage = Environment.getExternalStorageDirectory().getAbsolutePath() +  File.separator + myContext.getResources().getString(R.string.folder_name);
            String filePath = combinePath(extStorage, filename + ".pdf");
            createEmptyFile(filePath);
            PdfWriter.getInstance(document,new FileOutputStream(filePath));

            document.open();

            PdfPTable table = new PdfPTable(4); // 3 columns.

            PdfPCell cell1 = new PdfPCell(new Paragraph(myContext.getString(R.string.date)));
            PdfPCell cell2 = new PdfPCell(new Paragraph(myContext.getString(R.string.sent_rcv)));
            PdfPCell cell3 = new PdfPCell(new Paragraph(myContext.getString(R.string.details)));
            PdfPCell cell4 = new PdfPCell(new Paragraph(myContext.getString(R.string.amount)));

            table.addCell(cell1);
            table.addCell(cell2);
            table.addCell(cell3);
            table.addCell(cell4);

            for ( int i = 0 ; i < myTransactions.size() ; i++ )
            {
                table.completeRow();

                TransactionDetails td = myTransactions.get(i);

                String dateText = String.format("%s\n%s\n%s",td.getDateString(),td.getTimeString(),td.getTimeZone());
                PdfPCell dateCell = new PdfPCell(new Paragraph(dateText));
                table.addCell(dateCell);

                Drawable d;
                if (td.getSent())
                {
                    d = ContextCompat.getDrawable(myContext,R.drawable.sendicon);
                }
                else
                {
                    d = ContextCompat.getDrawable(myContext,R.drawable.rcvicon);
                }

                try {
                    BitmapDrawable drawable = (BitmapDrawable) d;
                    Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] imageInByte = stream.toByteArray();
                    Image myImage = Image.getInstance(imageInByte);
                    myImage.scalePercent(25);
                    PdfPCell sendReceiveCell = new PdfPCell(myImage,false);
                    sendReceiveCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    sendReceiveCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    table.addCell(sendReceiveCell);
                }
                catch(IOException ex)
                {
                    //return;
                }

                String from = myContext.getString(R.string.from_capital);
                String to = myContext.getString(R.string.to_capital);
                String memo = myContext.getString(R.string.memo_capital);


                String detailsText = String.format(""+to+": %s\n"+from+": %s\n"+memo+": %s",td.getDetailsTo(),td.getDetailsFrom(),td.getDetailsMemo());

                PdfPCell detailsCell = new PdfPCell(new Paragraph(detailsText));
                table.addCell(detailsCell);

                String amountText;
                if ( td.getSent() )
                {
                    amountText = String.format("- %s %s\n- %s %s",Float.toString(td.getAmount()),td.getAssetSymbol(),Float.toString(td.getFaitAmount()),td.getFaitAssetSymbol());
                }
                else
                {
                    amountText = String.format("+ %s %s\n+ %s %s",Float.toString(td.getAmount()),td.getAssetSymbol(),Float.toString(td.getFaitAmount()),td.getFaitAssetSymbol());
                }
                PdfPCell amountsCell = new PdfPCell(new Paragraph(amountText));
                amountsCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(amountsCell);

            }

            document.add(table);
            document.close();
//            Toast.makeText(myContext, myContext.getText(R.string.pdf_generated_msg) + filePath, Toast.LENGTH_LONG).show();
            Toast.makeText(myContext, myContext.getText(R.string.pdf_generated_msg) + filePath, Toast.LENGTH_LONG).show();
            Toast.makeText(myContext, myContext.getText(R.string.pdf_generated_msg) + filePath, Toast.LENGTH_LONG).show();
        }
        catch(Exception e){
            Toast.makeText(myContext, myContext.getText(R.string.pdf_generated_msg_error) + e.getMessage(), Toast.LENGTH_LONG).show();

        }
    }
    public void createTransactionpdf (HashMap<String,String> map, ImageView imageView)
    {

        Document document = new Document();

        try {
            String extStorage = Environment.getExternalStorageDirectory().getAbsolutePath();
            String filePath = combinePath(extStorage, filename + ".pdf");
            createEmptyFile(filePath);
            PdfWriter.getInstance(document,new FileOutputStream(filePath));

            document.open();

            if(imageView!=null) {
                try {
                    PdfPTable table1 = new PdfPTable(1); // 2 columns.
                    imageView.buildDrawingCache();
                    Bitmap bitmap = imageView.getDrawingCache();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] imageInByte = stream.toByteArray();
                    Image myImage = Image.getInstance(imageInByte);
                    myImage.scalePercent(25);
                    PdfPCell sendReceiveCell = new PdfPCell(myImage, false);
                    sendReceiveCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    sendReceiveCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    table1.addCell(sendReceiveCell);
                    table1.completeRow();
                    document.add(table1);
                } catch (Exception e) {

                }
            }
            PdfPTable table = new PdfPTable(1); // 2 columns.
            PdfPCell cell1 = new PdfPCell(new Paragraph(myContext.getString(R.string.raw_transactions)));
            table.addCell(cell1);
            table.completeRow();

            document.add(table);

            String amount = myContext.getString(R.string.amount);
            String symbol = myContext.getString(R.string.symbol);


            document.add(addforCell(myContext.getString(R.string.id_s),map.get("id")));
            document.add(addforCell(myContext.getString(R.string.time),map.get("time")));
            document.add(addforCell(myContext.getString(R.string.trx_in_block),map.get("trx_in_block")));
            document.add(addforCell(myContext.getString(R.string.operations),"----"));
            String detailsFee = String.format(""+amount+": %s\n"+symbol+"symbol: %s",map.get("amountFee"),map.get("symbolFee"));
            document.add(addforCell(myContext.getString(R.string.fee),detailsFee));
            document.add(addforCell(myContext.getString(R.string.from_capital),map.get("from")));
            document.add(addforCell(myContext.getString(R.string.to_capital),map.get("to")));
            String detailsAmount = String.format(""+amount+": %s\n"+symbol+"symbol: %s",map.get("amountAmount"),map.get("symbolAmount"));
            document.add(addforCell(myContext.getString(R.string.amount),detailsAmount));
            document.add(addforCell(myContext.getString(R.string.memo_capital),map.get("memo")));
            document.add(addforCell(myContext.getString(R.string.extensions),map.get("extensions")));
            document.add(addforCell(myContext.getString(R.string.op_in_trx),map.get("op_in_trx")));
            document.add(addforCell(myContext.getString(R.string.virtual_op),map.get("virtual_op")));
            document.add(addforCell(myContext.getString(R.string.operation_results),map.get("operation_results")));


            document.close();

            Intent email = new Intent(Intent.ACTION_SEND);
            email.putExtra(Intent.EXTRA_EMAIL, "---");
            email.putExtra(Intent.EXTRA_SUBJECT, "---");
            email.putExtra(Intent.EXTRA_TEXT, "---");
            Uri uri = Uri.fromFile(new File(extStorage, filename + ".pdf"));
            email.putExtra(Intent.EXTRA_STREAM, uri);
            email.setType("application/pdf");
            email.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            myContext.startActivity(email);
        }
        catch(Exception e){
            Toast.makeText(myContext, myContext.getText(R.string.pdf_generated_msg_error) + e.getMessage(), Toast.LENGTH_LONG).show();

        }
    }
    PdfPTable addforCell(String subject , String detail){
        PdfPTable table = new PdfPTable(2); // 2 columns.
        PdfPCell cell1 = new PdfPCell(new Paragraph(subject));
        PdfPCell cell2 = new PdfPCell(new Paragraph(detail));
        table.addCell(cell1);
        table.addCell(cell2);
        table.completeRow();
        return table;
    }
}
