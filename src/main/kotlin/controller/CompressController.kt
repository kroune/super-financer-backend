package controller

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

class CompressController {
//    fun compress(image: ByteArray): BufferedImage? {
//        val byteArray = ByteArrayInputStream(image)
//        val inputImage = ImageIO.read(byteArray)
//
//        val imageInputStream = ImageIO.createImageInputStream(byteArray)
//        val imageReader = ImageIO.getImageReaders(imageInputStream).next()
//        ImageIO.getImageWriter(imageReader)
//        val writers = ImageIO.getImageWritersByFormatName("jpg")
//        val writer = writers.next()
//
//        val outputFile = File("output.jpg")
//        val outputStream = ImageIO.createImageOutputStream(outputFile)
//        writer.setOutput(outputStream)
//
//        val params = writer.defaultWriteParam
//        params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT)
//        params.setCompressionQuality(0.5f)
//
//        val baos = ByteArrayOutputStream()
//        ImageIO.write(img, "jpg", baos)
//        val bytes = baos.toByteArray()
//
//        IIOImage()
//        writer.write(null, IIOImage(inputImage, null, null), params)
//
//        outputStream.close()
//        writer.dispose()
//        return inputImage
//    }
}
