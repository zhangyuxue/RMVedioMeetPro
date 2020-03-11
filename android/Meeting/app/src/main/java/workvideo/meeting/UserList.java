package workvideo.meeting;

public class UserList
{
    public class Record
    {
        public long _uid;
        public String _uri;

        public Record(long uid, String uri)
        {
            _uid =uid;
            _uri =uri;
        }
    }

    Record[] _table = new Record[100];
    int _count =0;

    public void add(long uid, String uri)
    {
        for(int i=0; i<_count; i++)
            if(_table[i]._uid ==uid)
            {
                _table[i]._uri =uri;
                return;
            }

        if(_count == _table.length)
        {
            // grow table
            Record[] new_table =new Record[_count<<1];
            for (int i=0; i<_count ;i++)
                new_table[i] =_table[i];
            _table =new_table;
        }
        _table[_count++] =new Record(uid,uri);
    }
    public void remove(long uid)
    {
        for (int i=0; i<_count; i++)
        {
            if (_table[i]._uid ==uid)
            {
                _count--;
                while (i < _count)
                {
                    _table[i]=_table[i+1];
                    i++;
                }
                _table[i] =null;
                break;
            }
        }
    }
    public Record getAt(int index)
    {
        return _table[index];
    }
    public int count()
    {
        return _count;
    }
    public String getUri(long uid)
    {
        for (int i=0; i<_count; i++)
            if(_table[i]._uid ==uid)
                return _table[i]._uri;
        return null;
    }
}
