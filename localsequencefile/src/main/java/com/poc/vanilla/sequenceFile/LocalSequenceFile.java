package com.poc.vanilla.sequenceFile;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class LocalSequenceFile {
    /**
     * the object of this class intends to be not thread-safe !!!
     */
    public static class Writer {
        private Text key = new Text();
        private BytesWritable value = new BytesWritable();
        private SequenceFile.Writer writer;

        public Writer(String outfilePath) throws IOException {
            Configuration conf = new Configuration();
            FileSystem fs = FileSystem.getLocal(conf);
            Path outFile = new Path(outfilePath);

            this.writer = SequenceFile.createWriter(conf, SequenceFile.Writer.file(outFile),
                    SequenceFile.Writer.keyClass(key.getClass()), SequenceFile.Writer.valueClass(value.getClass()));
        }

        public void write(String key, byte[] value) throws IOException{
            this.key.set(key);
            this.value.set(value, 0, value.length);
            writer.append(this.key, this.value);
        }

        public void close() throws IOException{
            if(writer != null) {
                writer.close();
            }
        }
    }

    public interface RecordHandler {
        void handleRecord(String key, byte[] value);
    }

    public static void read(String sequenceFile, RecordHandler handler) throws IOException {
        SequenceFile.Reader reader = null;

        try {
            Configuration conf = new Configuration();
            Text key = new Text();
            BytesWritable value = new BytesWritable();

            reader = new SequenceFile.Reader(conf, SequenceFile.Reader.file(new Path(sequenceFile)), SequenceFile.Reader.bufferSize(4096));
            while(reader.next(key, value)) {
                handler.handleRecord(key.toString(), value.getBytes());
            }
        }
        finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.printf("Usage: %s needs two arguments   files\n",
                    LocalSequenceFile.class.getSimpleName());
            System.exit(-1);
        }

        File workDirectory = new File(".");
        System.setProperty("hadoop.home.dir", workDirectory.getAbsolutePath());
        System.setProperty("hadoop_home", workDirectory.getAbsolutePath());

        ////////////////////////////////////////////////////////
        // test writing to local sequence file
        ////////////////////////////////////////////////////////

        // path to input file – in local file system
        File inFile = new File(args[0]);

        // Path for output file
        String outFile = args[1];

        Writer writer = null;

        try {
            writer = new Writer(outFile);
            int i =0;
            for (String line : FileUtils.readLines(inFile, Charset.defaultCharset())){
                String key = "" + i;
                writer.write(key, line.getBytes());
                i++;
            }
        } finally {
            if(writer!=null)
                writer.close();
        }

        ////////////////////////////////////////////////////////
        // test reading local sequence file
        ////////////////////////////////////////////////////////
        read(outFile, (key, value) -> System.out.printf("Key %s : Value %s \n", key, new String(value)));
    }
}
