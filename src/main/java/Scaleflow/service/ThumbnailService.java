package Scaleflow.service;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.stereotype.Service;

@Service 
public class ThumbnailService {
    public String generateThumbnail(String inputPath,Long jobId)
            throws IOException{

        if(inputPath==null||inputPath.isBlank()){
            throw new IllegalArgumentException("Input path is required");
        }

        BufferedImage original =ImageIO.read(Path.of(inputPath).toFile());

        if(original==null){
            throw new IOException("Unsupported image: "+inputPath);
        }

        int width=original.getWidth();
        int height=original.getHeight();

        double scale=Math.min(1.0,300.0/Math.max(width,height));

        int newHeight=Math.max(1,(int) Math.round(height*scale));
        int newWidth= Math.max(1,(int) Math.round(width*scale));

        BufferedImage thumbnImage=new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics=thumbnImage.createGraphics();

        try{
            graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );
            graphics.drawImage(original, 0, 0, newWidth, newHeight, null);
        } finally {
            graphics.dispose();
        }

        Path outputDirectory=Path.of("outputs");
        Files.createDirectories(outputDirectory);

        Path outputPath=outputDirectory.resolve(jobId+"_thumbnail.png");

        boolean written=ImageIO.write(thumbnImage, "png", outputPath.toFile());

        if(!written){
            throw new IOException("Unable to write PNG image");
        }

        return outputPath.toAbsolutePath().toString();
    }
}
