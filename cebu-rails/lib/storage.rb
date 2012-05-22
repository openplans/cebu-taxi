class Storage

  def initialize
    @files = {}
    @path = CebuRails::Application.config.csv_file_path
    if !File.exist? @path
      Dir.mkdir @path
    end
  end

  def store(filename, *args)
    if @files[filename].nil?
      @files[filename] = File.open(File.join(@path, filename), 'a')
    end
    file = @files[filename]
    file.write(args.join(','))
    file.write('\n')
    file.flush
  end
  
  @@instance = Storage.new

  def self.store(file, *args)
    @@instance.store(file, *args)
  end

  private_class_method :new
end



