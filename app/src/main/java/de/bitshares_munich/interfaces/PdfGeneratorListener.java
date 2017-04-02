package de.bitshares_munich.interfaces;

/**
 * Interface to be implemented by the entity insterested in get updates from the PdfGeneratorTask.
 * Created by nelson on 12/28/16.
 */
public interface PdfGeneratorListener {

    /**
     * Called when we have an update on the progress of the operation.
     *
     * @param percentage
     */
    public void onUpdate(float percentage);

    /**
     * Called to signal the end of the conversion procedure.
     */
    public void onReady(String message);

    /**
     * Called to signal an error while creating the PDF
     */
    public void onError(String message);
}
